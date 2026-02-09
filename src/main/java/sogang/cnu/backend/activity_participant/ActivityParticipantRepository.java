package sogang.cnu.backend.activity_participant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
    Optional<ActivityParticipant> findByUserIdAndActivityId(Long userId, Long activityId);
    List<ActivityParticipant> findByUserId(Long userId);
}
