package sogang.cnu.backend.lecture_material;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.lecture_material.dto.LectureMaterialRequestDto;
import sogang.cnu.backend.lecture_material.dto.LectureMaterialResponseDto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LectureMaterialService {
    private static final Set<String> GOOGLE_DRIVE_HOSTS = Set.of(
            "drive.google.com",
            "docs.google.com"
    );

    private final LectureMaterialRepository lectureMaterialRepository;

    @Transactional(readOnly = true)
    public List<LectureMaterialResponseDto> getAll() {
        return lectureMaterialRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public LectureMaterialResponseDto create(LectureMaterialRequestDto request) {
        String driveUrl = validateAndNormalizeDriveUrl(request.getDriveUrl());
        LectureMaterial material = LectureMaterial.builder()
                .title(request.getTitle().trim())
                .description(normalizeDescription(request.getDescription()))
                .driveUrl(driveUrl)
                .build();
        return toResponseDto(lectureMaterialRepository.save(material));
    }

    @Transactional
    public LectureMaterialResponseDto update(UUID id, LectureMaterialRequestDto request) {
        LectureMaterial material = findOrThrow(id);
        material.update(
                request.getTitle().trim(),
                normalizeDescription(request.getDescription()),
                validateAndNormalizeDriveUrl(request.getDriveUrl())
        );
        return toResponseDto(material);
    }

    @Transactional
    public void delete(UUID id) {
        lectureMaterialRepository.delete(findOrThrow(id));
    }

    private LectureMaterial findOrThrow(UUID id) {
        return lectureMaterialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("강의자료를 찾을 수 없습니다."));
    }

    private String validateAndNormalizeDriveUrl(String value) {
        String normalized = value.trim();
        try {
            URI uri = new URI(normalized);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) ||
                    host == null ||
                    !GOOGLE_DRIVE_HOSTS.contains(host.toLowerCase()) ||
                    uri.getUserInfo() != null) {
                throw new BadRequestException("Google Drive 공유 링크를 확인해주세요.");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new BadRequestException("Google Drive 공유 링크를 확인해주세요.");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) return null;
        return description.trim();
    }

    private LectureMaterialResponseDto toResponseDto(LectureMaterial material) {
        return LectureMaterialResponseDto.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .driveUrl(material.getDriveUrl())
                .createdAt(material.getCreatedAt())
                .modifiedAt(material.getModifiedAt())
                .build();
    }
}
