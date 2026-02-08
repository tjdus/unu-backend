package sogang.cnu.backend.application;



import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByRecruitmentId(Long recruitmentId);
}