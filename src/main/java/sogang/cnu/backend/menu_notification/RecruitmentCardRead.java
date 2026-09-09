package sogang.cnu.backend.menu_notification;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "recruitment_card_reads",
        indexes = @Index(name = "idx_recruitment_card_reads_user", columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(
                name = "uk_recruitment_card_read_recruitment_user",
                columnNames = {"recruitment_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RecruitmentCardRead {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruitment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Recruitment recruitment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt;
}
