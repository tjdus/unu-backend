package sogang.cnu.backend.application;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByRecruitmentId(UUID recruitmentId);
    Optional<Application> findFirstByNameAndEmailOrderByCreatedAtDesc(String name, String email);

}