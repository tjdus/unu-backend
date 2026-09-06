package sogang.cnu.backend.activity_participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {
    Optional<ActivityParticipant> findByUserIdAndActivityId(UUID userId, UUID activityId);
    List<ActivityParticipant> findByUserId(UUID userId);
    List<ActivityParticipant> findByActivityId(UUID activityId);
    @Query("""
            SELECT ap.activity.id FROM ActivityParticipant ap
            WHERE ap.user.id = :userId
              AND ap.activity.quarter.id = :quarterId
              AND ap.resultChangedAt IS NOT NULL
              AND ap.resultReadAt IS NULL
            ORDER BY ap.resultChangedAt DESC
            """)
    List<UUID> findUnreadResultActivityIds(
            @Param("userId") UUID userId,
            @Param("quarterId") UUID quarterId
    );
    long countByActivityIdAndStatusIn(
            UUID activityId,
            Collection<ActivityParticipantStatus> statuses
    );
    long countByActivityIdAndStatusInAndUserIdNot(
            UUID activityId,
            Collection<ActivityParticipantStatus> statuses,
            UUID userId
    );

    default long countCapacityParticipants(
            Activity activity,
            Collection<ActivityParticipantStatus> statuses
    ) {
        if (activity.getAssignee() == null) {
            return countByActivityIdAndStatusIn(activity.getId(), statuses);
        }
        return countByActivityIdAndStatusInAndUserIdNot(
                activity.getId(), statuses, activity.getAssignee().getId());
    }

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.activity.quarter.id = :quarterId")
    List<ActivityParticipant> findByActivityQuarterId(@Param("quarterId") UUID quarterId);

    @Query("""
            SELECT ap FROM ActivityParticipant ap
            WHERE ap.status = :status
              AND ap.activity.startDate IS NOT NULL
              AND ap.activity.startDate <= :today
              AND ap.activity.status <> :excludedActivityStatus
              AND ap.activity.activityType.code <> 'PROJECT'
            """)
    List<ActivityParticipant> findReadyForConfirmation(
            @Param("status") ActivityParticipantStatus status,
            @Param("today") LocalDate today,
            @Param("excludedActivityStatus") ActivityStatus excludedActivityStatus
    );
}
