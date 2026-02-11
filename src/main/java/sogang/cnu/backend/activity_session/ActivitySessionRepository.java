package sogang.cnu.backend.activity_session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ActivitySessionRepository extends JpaRepository<ActivitySession, Long> {

}
