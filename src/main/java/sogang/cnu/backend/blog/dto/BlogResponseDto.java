package sogang.cnu.backend.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BlogResponseDto {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private String category;
    private AuditorDto createdBy;
    private String createdAt;
}
