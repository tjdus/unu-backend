package sogang.cnu.backend.lecture_material;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.activity.Activity;
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

    /** 링크에 표시할 자료 이름. 없으면 제목(내용 이름)을 그대로 쓴다. */
    @Column(name = "material_name", length = 120)
    private String materialName;

    @Column(name = "drive_url", nullable = false, length = 2048)
    private String driveUrl;

    @Column(name = "week_number")
    private Integer weekNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    public void update(String title, String description, String materialName, String driveUrl,
                       Integer weekNumber, Activity activity) {
        this.title = title;
        this.description = description;
        this.materialName = materialName;
        this.driveUrl = driveUrl;
        this.weekNumber = weekNumber;
        this.activity = activity;
    }
}
