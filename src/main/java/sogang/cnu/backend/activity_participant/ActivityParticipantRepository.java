package sogang.cnu.backend.activity_participant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, UUID> {
    Optional<ActivityParticipant> findByUserIdAndActivityId(UUID userId, UUID activityId);
    List<ActivityParticipant> findByUserId(UUID userId);
    List<ActivityParticipant> findByActivityId(UUID activityId);
}
