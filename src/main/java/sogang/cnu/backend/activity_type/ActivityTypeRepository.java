package sogang.cnu.backend.activity_type;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, UUID> {
}
