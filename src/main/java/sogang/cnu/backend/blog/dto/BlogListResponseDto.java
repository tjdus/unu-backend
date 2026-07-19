package sogang.cnu.backend.blog.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BlogListResponseDto {
    private List<BlogResponseDto> posts;
    private long total;
}
