package sogang.cnu.backend.quarter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CurrentQuarterRepository extends JpaRepository<CurrentQuarter, UUID> {
}
