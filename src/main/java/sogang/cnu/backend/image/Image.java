package sogang.cnu.backend.image;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String url;

    @Column(nullable = false)
    private String filename;

    private String originalName;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ImageStatus status = ImageStatus.TEMP;

    private UUID postId;

    @Enumerated(EnumType.STRING)
    private PostType postType;
}
