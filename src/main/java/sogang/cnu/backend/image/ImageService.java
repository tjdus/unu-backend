package sogang.cnu.backend.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.image.dto.ImageUploadResponseDto;
import sogang.cnu.backend.util.SecurityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Pattern IMAGE_URL_PATTERN =
            Pattern.compile("!\\[.*?]\\((https?://[^)\\s]+)\\)");

    // ── 업로드 이미지 검증 규칙 (jpg/jpeg/png/webp/gif 만 허용, svg/bmp/html 등 거부) ──
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024; // 10MB
    private static final int SIGNATURE_HEADER_LENGTH = 12; // WebP(RIFF....WEBP) 판별에 12바이트 필요
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    private enum ImageType { JPEG, PNG, WEBP, GIF }

    private final ImageRepository imageRepository;

    @Value("${app.image.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // -------------------------------------------------------------------------
    // Upload
    // -------------------------------------------------------------------------

    @Transactional
    public ImageUploadResponseDto upload(MultipartFile file) throws IOException {
        // 크기·확장자·MIME·매직바이트·상호일치 검증을 서비스에서 최종 수행한다.
        // (컨트롤러가 아닌 다른 호출자가 upload()를 써도 검증을 우회하지 못하도록 여기에 둔다.)
        String extension = validateImage(file);

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // 저장 파일명은 서버가 만든 UUID + 검증된 확장자만 사용한다(원본 파일명/경로는 사용하지 않음).
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadPath.resolve(filename).normalize();
        if (!target.startsWith(uploadPath)) {
            throw new BadRequestException("잘못된 파일 경로입니다.");
        }

        Files.copy(file.getInputStream(), target);

        String url = baseUrl + "/api/public/images/" + filename;

        Image image = Image.builder()
                .url(url)
                .filename(filename)
                .originalName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .status(ImageStatus.TEMP)
                .build();

        Image saved = imageRepository.save(image);

        return ImageUploadResponseDto.builder()
                .id(saved.getId())
                .url(saved.getUrl())
                .build();
    }

    /**
     * 업로드 파일을 검증하고 정규화된(소문자) 확장자를 반환한다.
     * 크기 → 확장자 allowlist → Content-Type allowlist → 매직바이트 → 세 값의 상호 일치까지
     * 모두 통과해야 하며, 하나라도 실패하면 {@link BadRequestException}을 던진다.
     */
    String validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("업로드할 이미지를 선택해주세요.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("이미지는 10MB 이하만 업로드할 수 있습니다.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("jpg, jpeg, png, webp 형식의 이미지만 업로드할 수 있습니다.");
        }

        String contentType = file.getContentType();
        String normalizedContentType =
                contentType == null ? null : contentType.toLowerCase(Locale.ROOT);
        if (normalizedContentType == null || !ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException("허용되지 않는 이미지 형식입니다.");
        }

        // Content-Type은 조작 가능하므로 실제 파일 시그니처(매직바이트)까지 확인하고,
        // 확장자·Content-Type·실제 내용이 모두 같은 형식을 가리키는지 상호 검증한다.
        ImageType byExtension = typeFromExtension(extension);
        ImageType byContentType = typeFromContentType(normalizedContentType);
        ImageType bySignature = detectSignatureType(readHeader(file, SIGNATURE_HEADER_LENGTH));
        if (bySignature == null || byExtension != bySignature || byContentType != bySignature) {
            throw new BadRequestException("이미지 형식과 실제 파일 내용이 일치하지 않습니다.");
        }

        return extension;
    }

    /** 원본 파일명에서 마지막 확장자만 소문자로 뽑는다. 확장자가 없으면 null. */
    private String extractExtension(String originalName) {
        if (originalName == null) {
            return null;
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return null;
        }
        return originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private ImageType typeFromExtension(String extension) {
        return switch (extension) {
            case "jpg", "jpeg" -> ImageType.JPEG;
            case "png" -> ImageType.PNG;
            case "webp" -> ImageType.WEBP;
            case "gif" -> ImageType.GIF;
            default -> null;
        };
    }

    private ImageType typeFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ImageType.JPEG;
            case "image/png" -> ImageType.PNG;
            case "image/webp" -> ImageType.WEBP;
            case "image/gif" -> ImageType.GIF;
            default -> null;
        };
    }

    /** 파일 앞부분 바이트로 실제 형식을 판별한다. 알 수 없으면 null. */
    private ImageType detectSignatureType(byte[] header) {
        if (isJpeg(header)) return ImageType.JPEG;
        if (isPng(header)) return ImageType.PNG;
        if (isWebp(header)) return ImageType.WEBP;
        if (isGif(header)) return ImageType.GIF;
        return null;
    }

    private boolean isJpeg(byte[] h) {
        // FF D8 FF
        return h.length >= 3
                && (h[0] & 0xFF) == 0xFF
                && (h[1] & 0xFF) == 0xD8
                && (h[2] & 0xFF) == 0xFF;
    }

    private boolean isPng(byte[] h) {
        // 89 50 4E 47 0D 0A 1A 0A
        return h.length >= 8
                && (h[0] & 0xFF) == 0x89 && (h[1] & 0xFF) == 0x50
                && (h[2] & 0xFF) == 0x4E && (h[3] & 0xFF) == 0x47
                && (h[4] & 0xFF) == 0x0D && (h[5] & 0xFF) == 0x0A
                && (h[6] & 0xFF) == 0x1A && (h[7] & 0xFF) == 0x0A;
    }

    private boolean isWebp(byte[] h) {
        // "RIFF"(0~3) .... "WEBP"(8~11)
        return h.length >= 12
                && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
    }

    private boolean isGif(byte[] h) {
        // "GIF87a" 또는 "GIF89a"
        return h.length >= 6
                && h[0] == 'G' && h[1] == 'I' && h[2] == 'F'
                && h[3] == '8' && (h[4] == '7' || h[4] == '9') && h[5] == 'a';
    }

    /** 시그니처 판별에 필요한 앞부분 바이트만 읽는다(전체를 메모리에 올리지 않음). */
    private byte[] readHeader(MultipartFile file, int length) throws IOException {
        try (InputStream in = file.getInputStream()) {
            byte[] buffer = new byte[length];
            int read = 0;
            while (read < length) {
                int r = in.read(buffer, read, length - read);
                if (r == -1) break;
                read += r;
            }
            return read == length ? buffer : Arrays.copyOf(buffer, read);
        }
    }

    // -------------------------------------------------------------------------
    // Sync (called on post create / update)
    // -------------------------------------------------------------------------

    /**
     * Activates images referenced in content, deactivates removed ones.
     * Returns the effective thumbnail URL (explicit or first content image).
     */
    @Transactional
    public String syncImages(UUID postId, PostType postType, String content, String explicitThumbnailUrl) {
        List<String> contentUrls = extractImageUrls(content);
        Set<String> referencedUrls = new LinkedHashSet<>(contentUrls);
        if (explicitThumbnailUrl != null && !explicitThumbnailUrl.isBlank()) {
            referencedUrls.add(explicitThumbnailUrl);
        }

        // 게시물이 저장되기 전에는 연결할 ID가 없으므로 이미지 상태를 바꾸지 않는다.
        if (postId == null) {
            return determineThumbnail(contentUrls, explicitThumbnailUrl);
        }

        // Activate TEMP images found in this content
        if (!referencedUrls.isEmpty()) {
            List<Image> toActivate = imageRepository.findByUrlInAndStatus(referencedUrls, ImageStatus.TEMP);
            toActivate.forEach(img -> {
                img.setStatus(ImageStatus.ACTIVE);
                img.setPostId(postId);
                img.setPostType(postType);
            });
            if (!toActivate.isEmpty()) imageRepository.saveAll(toActivate);
        }

        // Deactivate images that are no longer referenced by the post or its thumbnail
        List<Image> currentActive = imageRepository.findByPostIdAndPostType(postId, postType);
        List<Image> toDeactivate = currentActive.stream()
                .filter(img -> !referencedUrls.contains(img.getUrl()))
                .collect(Collectors.toList());
        toDeactivate.forEach(img -> {
            img.setStatus(ImageStatus.TEMP);
            img.setPostId(null);
            img.setPostType(null);
        });
        if (!toDeactivate.isEmpty()) imageRepository.saveAll(toDeactivate);

        // Determine thumbnail
        return determineThumbnail(contentUrls, explicitThumbnailUrl);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    @Transactional
    public void deleteById(UUID id) {
        imageRepository.findById(id).ifPresent(img -> {
            // 업로더 본인 또는 운영진만 삭제할 수 있다.
            SecurityUtils.requireOwnerOrManager(img.getCreatedBy(), "본인이 업로드한 이미지만 삭제할 수 있습니다.");
            deleteFile(img.getFilename());
            imageRepository.delete(img);
        });
    }

    @Transactional
    public void deleteByFilename(String filename) {
        imageRepository.findByUrl(baseUrl + "/api/public/images/" + filename)
                .ifPresent(img -> {
                    deleteFile(img.getFilename());
                    imageRepository.delete(img);
                });
        // Also physically delete if not in DB (legacy)
        deleteFile(filename);
    }

    /** Called when a post is permanently deleted — removes all linked images. */
    @Transactional
    public void deletePostImages(UUID postId, PostType postType) {
        List<Image> images = imageRepository.findByPostIdAndPostType(postId, postType);
        images.forEach(img -> deleteFile(img.getFilename()));
        imageRepository.deleteAll(images);
    }

    // -------------------------------------------------------------------------
    // Cleanup (called by scheduler)
    // -------------------------------------------------------------------------

    @Transactional
    public void cleanupTempImages() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        List<Image> old = imageRepository.findByStatusAndCreatedAtBefore(ImageStatus.TEMP, cutoff);
        log.info("Cleaning up {} TEMP images older than 1 hour", old.size());
        old.forEach(img -> deleteFile(img.getFilename()));
        imageRepository.deleteAll(old);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public List<String> extractImageUrls(String content) {
        if (content == null || content.isBlank()) return Collections.emptyList();
        List<String> urls = new ArrayList<>();
        Matcher m = IMAGE_URL_PATTERN.matcher(content);
        while (m.find()) {
            urls.add(m.group(1));
        }
        return urls;
    }

    private String determineThumbnail(List<String> contentUrls, String explicitThumbnailUrl) {
        if (explicitThumbnailUrl != null && !explicitThumbnailUrl.isBlank()) {
            return explicitThumbnailUrl;
        }
        return contentUrls.isEmpty() ? "" : contentUrls.get(0);
    }

    private void deleteFile(String filename) {
        try {
            Path path = Paths.get(uploadDir).toAbsolutePath().resolve(filename).normalize();
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (path.startsWith(base)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", filename, e.getMessage());
        }
    }
}
