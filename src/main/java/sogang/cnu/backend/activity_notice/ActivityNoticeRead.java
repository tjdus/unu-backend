package sogang.cnu.backend.activity_notice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import sogang.cnu.backend.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activity_notice_reads",
        indexes = @Index(
                name = "idx_activity_notice_reads_user",
                columnList = "user_id"
        ),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activity_notice_read_notice_user",
                columnNames = {"notice_id", "user_id"}
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityNoticeRead {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ActivityNotice notice;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
