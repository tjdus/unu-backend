package sogang.cnu.backend.quarter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuarterRepository extends JpaRepository<Quarter, UUID> {
    List<Quarter> findByYearAndSeason(int year, Season season);
    Optional<Quarter> findFirstByYearAndSeason(int year, Season season);

}
