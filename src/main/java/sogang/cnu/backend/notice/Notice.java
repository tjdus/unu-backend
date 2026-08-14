package sogang.cnu.backend.notice;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "notices")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String tag;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "notification_enabled", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean notificationEnabled = false;

    public void update(String title, String tag, String content) {
        this.title = title;
        this.tag = tag;
        this.content = content;
        this.notificationEnabled = true;
    }
}
