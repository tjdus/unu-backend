package sogang.cnu.backend.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetPlanRepository extends JpaRepository<BudgetPlan, UUID> {

    List<BudgetPlan> findByQuarterIdOrderByMonthAsc(UUID quarterId);

    Optional<BudgetPlan> findByQuarterIdAndMonth(UUID quarterId, Integer month);

    @Query("SELECT bp FROM BudgetPlan bp LEFT JOIN FETCH bp.items WHERE bp.quarter.id = :quarterId ORDER BY bp.month ASC")
    List<BudgetPlan> findByQuarterIdWithItems(@Param("quarterId") UUID quarterId);
}
