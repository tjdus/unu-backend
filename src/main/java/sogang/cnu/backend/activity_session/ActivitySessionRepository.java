package sogang.cnu.backend.activity_session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, UUID> {
    List<ActivitySession> findByActivityId(UUID activityId);

    Optional<ActivitySession> findFirstByActivityIdOrderBySessionNumberDesc(UUID activityId);
    boolean existsByActivityIdAndDate(UUID activityId, java.time.LocalDate date);
    boolean existsByActivityIdAndDateAndIdNot(UUID activityId, java.time.LocalDate date, UUID id);

}
