package sogang.cnu.backend.blog.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BlogRequestDto {
    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private String category;
}
