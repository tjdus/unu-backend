package sogang.cnu.backend.activity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID>, ActivityRepositoryCustom {

    List<Activity> findByAssigneeId(UUID assigneeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Activity a WHERE a.id = :id")
    Optional<Activity> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Activity a SET a.parentActivity = null WHERE a.parentActivity.id = :activityId")
    void detachChildActivities(@Param("activityId") UUID activityId);

    @Query("""
            SELECT a.id
            FROM Activity a
            WHERE a.quarter.id = :quarterId
              AND (a.listed IS NULL OR a.listed = true)
              AND a.recruitmentStartDate IS NOT NULL
              AND a.recruitmentEndDate IS NOT NULL
              AND a.createdAt >= :cutoff
              AND NOT EXISTS (
                SELECT receipt.id
                FROM ActivityCardRead receipt
                WHERE receipt.activity.id = a.id
                  AND receipt.user.id = :userId
              )
            """)
    List<UUID> findUnreadRecentListedActivityIds(
            @Param("quarterId") UUID quarterId,
            @Param("userId") UUID userId,
            @Param("cutoff") LocalDateTime cutoff
    );
}
