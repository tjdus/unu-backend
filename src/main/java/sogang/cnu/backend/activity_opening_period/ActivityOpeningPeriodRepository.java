package sogang.cnu.backend.activity_opening_period;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActivityOpeningPeriodRepository extends JpaRepository<ActivityOpeningPeriod, UUID> {
    Optional<ActivityOpeningPeriod> findByQuarterId(UUID quarterId);
}
