package sogang.cnu.backend.menu_notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface RecruitmentCardReadRepository extends JpaRepository<RecruitmentCardRead, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO recruitment_card_reads (id, recruitment_id, user_id, read_at)
            VALUES (:id, :recruitmentId, :userId, :readAt)
            ON CONFLICT (recruitment_id, user_id) DO NOTHING
            """, nativeQuery = true)
    void insertIgnore(
            @Param("id") UUID id,
            @Param("recruitmentId") UUID recruitmentId,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt
    );
}
