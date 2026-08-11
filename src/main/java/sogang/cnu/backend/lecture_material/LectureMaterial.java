package sogang.cnu.backend.lecture_material;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "lecture_materials")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureMaterial extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "drive_url", nullable = false, length = 2048)
    private String driveUrl;

    public void update(String title, String description, String driveUrl) {
        this.title = title;
        this.description = description;
        this.driveUrl = driveUrl;
    }
}
