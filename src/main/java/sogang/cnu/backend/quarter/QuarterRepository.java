package sogang.cnu.backend.quarter;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuarterRepository extends JpaRepository<Quarter, Long> {
    List<Quarter> findByYearAndSeason(int year, Season season);
}
