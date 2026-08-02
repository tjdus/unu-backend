package sogang.cnu.backend.blog;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.blog.command.BlogCreateCommand;
import sogang.cnu.backend.blog.command.BlogUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "blogs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Blog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    private BlogCategory category;

    public void update(BlogUpdateCommand command) {
        this.title = command.getTitle();
        this.subtitle = command.getSubtitle();
        this.description = command.getDescription();
        this.thumbnailUrl = command.getThumbnailUrl();
        this.category = command.getCategory();
    }

    public static Blog create(BlogCreateCommand command) {
        return Blog.builder()
                .title(command.getTitle())
                .subtitle(command.getSubtitle())
                .description(command.getDescription())
                .thumbnailUrl(command.getThumbnailUrl())
                .category(command.getCategory())
                .build();
    }
}
