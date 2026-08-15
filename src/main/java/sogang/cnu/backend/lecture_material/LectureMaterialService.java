package sogang.cnu.backend.lecture_material;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityAccessGuard;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.lecture_material.dto.LectureMaterialRequestDto;
import sogang.cnu.backend.lecture_material.dto.LectureMaterialResponseDto;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureMaterialService {
    private static final Set<String> GOOGLE_DRIVE_HOSTS = Set.of(
            "drive.google.com",
            "docs.google.com"
    );
    private static final Set<String> NOTION_HOSTS = Set.of(
            "notion.so",
            "notion.site"
    );

    private final LectureMaterialRepository lectureMaterialRepository;
    private final ActivityRepository activityRepository;
    private final ActivityAccessGuard activityAccessGuard;

    @Transactional(readOnly = true)
    public List<LectureMaterialResponseDto> getAll() {
        return lectureMaterialRepository.findAllByOrderByDisplayOrderAscCreatedAtDesc().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public void reorder(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        List<LectureMaterial> materials = lectureMaterialRepository.findAllById(orderedIds);
        Map<UUID, LectureMaterial> byId = materials.stream()
                .collect(Collectors.toMap(LectureMaterial::getId, material -> material));
        int order = 0;
        for (UUID id : orderedIds) {
            LectureMaterial material = byId.get(id);
            if (material == null) {
                throw new NotFoundException("강의자료를 찾을 수 없습니다.");
            }
            material.updateDisplayOrder(order++);
        }
    }

    @Transactional(readOnly = true)
    public List<LectureMaterialResponseDto> getByActivityId(UUID activityId) {
        return lectureMaterialRepository.findAllByActivityIdOrderByCreatedAtDesc(activityId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public LectureMaterialResponseDto create(LectureMaterialRequestDto request) {
        String driveUrl = validateAndNormalizeMaterialUrl(request.getDriveUrl());
        Activity activity = findActivity(request.getActivityId());
        requireManageable(activity);
        LectureMaterial material = LectureMaterial.builder()
                .title(request.getTitle().trim())
                .description(normalizeDescription(request.getDescription()))
                .materialName(normalizeMaterialName(request.getMaterialName()))
                .driveUrl(driveUrl)
                .weekNumber(request.getWeekNumber())
                .activity(activity)
                .build();
        return toResponseDto(lectureMaterialRepository.save(material));
    }

    @Transactional
    public LectureMaterialResponseDto update(UUID id, LectureMaterialRequestDto request) {
        LectureMaterial material = findOrThrow(id);
        requireManageable(material.getActivity());
        Activity activity = findActivity(request.getActivityId());
        requireManageable(activity);
        material.update(
                request.getTitle().trim(),
                normalizeDescription(request.getDescription()),
                normalizeMaterialName(request.getMaterialName()),
                validateAndNormalizeMaterialUrl(request.getDriveUrl()),
                request.getWeekNumber(),
                activity
        );
        return toResponseDto(material);
    }

    @Transactional
    public void delete(UUID id) {
        LectureMaterial material = findOrThrow(id);
        requireManageable(material.getActivity());
        lectureMaterialRepository.delete(material);
    }

    /** 활동 생성/수정 폼에서 입력한 대표 자료를 기존 자료 목록과 함께 관리한다. */
    @Transactional
    public void syncPrimaryMaterial(Activity activity, String driveUrl) {
        LectureMaterial existing = lectureMaterialRepository
                .findFirstByActivityIdAndPrimaryTrue(activity.getId())
                .orElse(null);
        String title = primaryMaterialTitle(activity);

        if (title == null || driveUrl == null || driveUrl.isBlank()) {
            if (existing != null) lectureMaterialRepository.delete(existing);
            return;
        }

        String normalizedUrl = validateAndNormalizeMaterialUrl(driveUrl);
        if (existing == null) {
            lectureMaterialRepository.save(LectureMaterial.builder()
                    .title(title)
                    .materialName(title)
                    .driveUrl(normalizedUrl)
                    .weekNumber(null)
                    .activity(activity)
                    .primary(true)
                    .build());
            return;
        }
        existing.updatePrimary(title, normalizedUrl, activity);
    }

    public String normalizeOptionalMaterialUrl(String value) {
        if (value == null || value.isBlank()) return null;
        return validateAndNormalizeMaterialUrl(value);
    }

    private void requireManageable(Activity activity) {
        activityAccessGuard.requireManage(activity, "해당 자료를 관리할 권한이 없습니다.");
    }

    private LectureMaterial findOrThrow(UUID id) {
        return lectureMaterialRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("강의자료를 찾을 수 없습니다."));
    }

    private Activity findActivity(UUID activityId) {
        if (activityId == null) return null;
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("연결할 활동을 찾을 수 없습니다."));
    }

    private String validateAndNormalizeMaterialUrl(String value) {
        String normalized = value.trim();
        if (normalized.length() > 2048) {
            throw new BadRequestException("자료 링크가 너무 깁니다.");
        }
        try {
            URI uri = new URI(normalized);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) ||
                    host == null ||
                    !isAllowedMaterialHost(host) ||
                    uri.getUserInfo() != null) {
                throw new BadRequestException("Google Drive 또는 Notion 공유 링크를 확인해주세요.");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new BadRequestException("Google Drive 또는 Notion 공유 링크를 확인해주세요.");
        }
    }

    private boolean isAllowedMaterialHost(String host) {
        String normalizedHost = host.toLowerCase();
        if (GOOGLE_DRIVE_HOSTS.contains(normalizedHost)) return true;
        return NOTION_HOSTS.stream().anyMatch(allowed ->
                normalizedHost.equals(allowed) || normalizedHost.endsWith("." + allowed)
        );
    }

    private String primaryMaterialTitle(Activity activity) {
        return switch (activity.getActivityType().getCode()) {
            case "STUDY" -> "스터디 자료";
            case "SPECIAL_LECTURE" -> "강의자료";
            default -> null;
        };
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) return null;
        return description.trim();
    }

    private String normalizeMaterialName(String materialName) {
        if (materialName == null || materialName.isBlank()) return null;
        return materialName.trim();
    }

    private LectureMaterialResponseDto toResponseDto(LectureMaterial material) {
        return LectureMaterialResponseDto.builder()
                .id(material.getId())
                .title(material.getTitle())
                .description(material.getDescription())
                .materialName(material.getMaterialName())
                .driveUrl(material.getDriveUrl())
                .weekNumber(material.getWeekNumber())
                .displayOrder(material.getDisplayOrder())
                .primary(Boolean.TRUE.equals(material.getPrimary()))
                .activityId(material.getActivity() == null ? null : material.getActivity().getId())
                .activityTitle(material.getActivity() == null ? null : material.getActivity().getTitle())
                .createdAt(material.getCreatedAt())
                .modifiedAt(material.getModifiedAt())
                .build();
    }
}
