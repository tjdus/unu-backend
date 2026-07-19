package sogang.cnu.backend.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class BlogResponseDto {
    private UUID id;
    private String title;
    private String subtitle;
    private String content;
    private String thumbnailUrl;
    private String category;
    private String createdBy;
    private String createdAt;
}
