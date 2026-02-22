package sogang.cnu.backend.activity_session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, UUID> {
    List<ActivitySession> findByActivityId(UUID activityId);

}
