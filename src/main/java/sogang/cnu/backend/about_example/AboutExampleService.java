package sogang.cnu.backend.about_example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.about_example.dto.AboutExampleRequestDto;
import sogang.cnu.backend.about_example.dto.AboutExampleResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.image.ImageService;
import sogang.cnu.backend.image.PostType;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AboutExampleService {

    private final AboutExampleRepository aboutExampleRepository;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public List<AboutExampleResponseDto> getByCategory(AboutExampleCategory category) {
        if (category == null) {
            throw new BadRequestException("소개 항목을 확인해 주세요.");
        }
        return aboutExampleRepository.findAllByCategoryOrderByDisplayOrderAscCreatedAtDesc(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void reorder(List<UUID> orderedIds) {
        requireManager();
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        List<AboutExample> examples = aboutExampleRepository.findAllById(orderedIds);
        Map<UUID, AboutExample> byId = examples.stream()
                .collect(Collectors.toMap(AboutExample::getId, example -> example));
        int order = 0;
        for (UUID id : orderedIds) {
            AboutExample example = byId.get(id);
            if (example == null) {
                throw new NotFoundException("소개 글을 찾을 수 없습니다.");
            }
            example.updateDisplayOrder(order++);
        }
    }

    @Transactional(readOnly = true)
    public AboutExampleResponseDto getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public AboutExampleResponseDto create(AboutExampleRequestDto dto) {
        requireManager();
        validate(dto);

        String thumbnailUrl = imageService.syncImages(
                null, PostType.ABOUT_EXAMPLE, "", dto.getThumbnailUrl().trim()
        );
        AboutExample example = AboutExample.builder()
                .category(dto.getCategory())
                .title(dto.getTitle().trim())
                .description(dto.getDescription().trim())
                .thumbnailUrl(thumbnailUrl)
                .build();
        AboutExample saved = aboutExampleRepository.save(example);
        imageService.syncImages(saved.getId(), PostType.ABOUT_EXAMPLE, "", thumbnailUrl);
        return toResponse(saved);
    }

    @Transactional
    public AboutExampleResponseDto update(UUID id, AboutExampleRequestDto dto) {
        requireManager();
        validate(dto);

        AboutExample example = findById(id);
        String thumbnailUrl = imageService.syncImages(
                id, PostType.ABOUT_EXAMPLE, "", dto.getThumbnailUrl().trim()
        );
        example.setCategory(dto.getCategory());
        example.setTitle(dto.getTitle().trim());
        example.setDescription(dto.getDescription().trim());
        example.setThumbnailUrl(thumbnailUrl);
        return toResponse(example);
    }

    @Transactional
    public void delete(UUID id) {
        requireManager();
        AboutExample example = findById(id);
        imageService.deletePostImages(id, PostType.ABOUT_EXAMPLE);
        aboutExampleRepository.delete(example);
    }

    private AboutExample findById(UUID id) {
        return aboutExampleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("소개 글을 찾을 수 없습니다."));
    }

    private void validate(AboutExampleRequestDto dto) {
        if (dto == null || dto.getCategory() == null) {
            throw new BadRequestException("소개 항목을 확인해 주세요.");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new BadRequestException("제목을 입력해 주세요.");
        }
        if (dto.getTitle().trim().length() > 120) {
            throw new BadRequestException("제목은 120자 이하로 입력해 주세요.");
        }
        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new BadRequestException("설명을 입력해 주세요.");
        }
        if (dto.getThumbnailUrl() == null || dto.getThumbnailUrl().isBlank()) {
            throw new BadRequestException("대표 이미지를 등록해 주세요.");
        }
    }

    private void requireManager() {
        if (!SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("소개 글을 관리할 권한이 없습니다.");
        }
    }

    private AboutExampleResponseDto toResponse(AboutExample example) {
        return AboutExampleResponseDto.builder()
                .id(example.getId())
                .category(example.getCategory())
                .title(example.getTitle())
                .description(example.getDescription())
                .thumbnailUrl(example.getThumbnailUrl())
                .createdAt(example.getCreatedAt())
                .modifiedAt(example.getModifiedAt())
                .build();
    }
}
