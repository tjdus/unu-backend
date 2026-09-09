package sogang.cnu.backend.about_example;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "about_examples")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AboutExample extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AboutExampleCategory category;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 2048)
    private String thumbnailUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
