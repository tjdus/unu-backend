package sogang.cnu.backend.applicant_notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ManagerApplicantCheckpointRepository extends JpaRepository<ManagerApplicantCheckpoint, UUID> {

    Optional<ManagerApplicantCheckpoint> findByUserId(UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO manager_applicant_checkpoints (id, user_id, last_checked_at)
            VALUES (:id, :userId, :checkedAt)
            ON CONFLICT (user_id) DO UPDATE SET last_checked_at = :checkedAt
            """, nativeQuery = true)
    void upsertLastCheckedAt(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("checkedAt") LocalDateTime checkedAt
    );
}
