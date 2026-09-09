package sogang.cnu.backend.blog.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.blog.BlogCategory;

@Getter
@Builder
public class BlogUpdateCommand {
    private String title;
    private String subtitle;
    private String description;
    private String thumbnailUrl;
    private BlogCategory category;
}
