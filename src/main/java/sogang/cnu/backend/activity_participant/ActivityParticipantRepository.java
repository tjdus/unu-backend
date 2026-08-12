package sogang.cnu.backend.activity_participant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sogang.cnu.backend.activity.Activity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {
    Optional<ActivityParticipant> findByUserIdAndActivityId(UUID userId, UUID activityId);
    List<ActivityParticipant> findByUserId(UUID userId);
    List<ActivityParticipant> findByActivityId(UUID activityId);
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
        return activity.getAssignee() != null
                ? countByActivityIdAndStatusInAndUserIdNot(
                        activity.getId(), statuses, activity.getAssignee().getId())
                : countByActivityIdAndStatusIn(activity.getId(), statuses);
    }

    @Query("SELECT ap FROM ActivityParticipant ap WHERE ap.activity.quarter.id = :quarterId")
    List<ActivityParticipant> findByActivityQuarterId(@Param("quarterId") UUID quarterId);

    @Query("""
            SELECT ap FROM ActivityParticipant ap
            WHERE ap.status = :status
              AND ap.activity.startDate IS NOT NULL
              AND ap.activity.startDate <= :today
            """)
    List<ActivityParticipant> findReadyForConfirmation(
            @Param("status") ActivityParticipantStatus status,
            @Param("today") LocalDate today
    );
}
