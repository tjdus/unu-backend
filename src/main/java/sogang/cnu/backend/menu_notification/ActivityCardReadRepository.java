package sogang.cnu.backend.menu_notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ActivityCardReadRepository extends JpaRepository<ActivityCardRead, UUID> {
    @Modifying
    @Query(value = """
            INSERT INTO activity_card_reads (id, activity_id, user_id, read_at)
            VALUES (:id, :activityId, :userId, :readAt)
            ON CONFLICT (activity_id, user_id) DO NOTHING
            """, nativeQuery = true)
    void insertIgnore(
            @Param("id") UUID id,
            @Param("activityId") UUID activityId,
            @Param("userId") UUID userId,
            @Param("readAt") LocalDateTime readAt
    );
}
