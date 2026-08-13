package sogang.cnu.backend.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.image.dto.ImageUploadResponseDto;
import sogang.cnu.backend.util.SecurityUtils;

import java.io.IOException;
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
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(uploadPath);

        String originalName = file.getOriginalFilename();
        String ext = (originalName != null && originalName.contains("."))
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String filename = UUID.randomUUID() + ext;

        Files.copy(file.getInputStream(), uploadPath.resolve(filename));

        String url = baseUrl + "/api/public/images/" + filename;

        Image image = Image.builder()
                .url(url)
                .filename(filename)
                .originalName(originalName)
                .fileSize(file.getSize())
                .status(ImageStatus.TEMP)
                .build();

        Image saved = imageRepository.save(image);

        return ImageUploadResponseDto.builder()
                .id(saved.getId())
                .url(saved.getUrl())
                .build();
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
