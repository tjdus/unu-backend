package sogang.cnu.backend.applicant_notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "manager_applicant_checkpoints",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_manager_applicant_checkpoint_user",
                columnNames = "user_id"
        )
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerApplicantCheckpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "last_checked_at", nullable = false)
    private LocalDateTime lastCheckedAt;
}
