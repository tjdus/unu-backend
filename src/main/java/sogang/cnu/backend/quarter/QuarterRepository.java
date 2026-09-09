package sogang.cnu.backend.quarter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QuarterRepository extends JpaRepository<Quarter, UUID> {
    List<Quarter> findByYearAndSeason(int year, Season season);
    Optional<Quarter> findFirstByYearAndSeason(int year, Season season);
    Optional<Quarter> findFirstByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate date1, LocalDate date2);

    @Query("""
            SELECT COUNT(a)
            FROM Activity a
            WHERE a.quarter.id = :quarterId
              AND (a.startDate < :startDate OR a.endDate > :endDate)
            """)
    long countActivitiesOutsidePeriod(
            @Param("quarterId") UUID quarterId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
