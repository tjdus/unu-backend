package sogang.cnu.backend.activity_session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, Long> {
    List<ActivitySession> findByActivityId(Long activityId);

}
