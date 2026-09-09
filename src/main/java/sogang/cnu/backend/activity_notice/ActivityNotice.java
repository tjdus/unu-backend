package sogang.cnu.backend.activity_notice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

/** 개별 활동 안에서만 쓰는 공지. 홈 화면의 학회 공지(Notice)와는 별개다. */
@Entity
@Table(name = "activity_notices")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNotice extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
