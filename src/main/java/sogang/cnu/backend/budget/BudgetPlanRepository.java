package sogang.cnu.backend.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetPlanRepository extends JpaRepository<BudgetPlan, UUID> {

    List<BudgetPlan> findByQuarterIdOrderByMonthAsc(UUID quarterId);

    Optional<BudgetPlan> findByQuarterIdAndMonth(UUID quarterId, Integer month);

    @Query("SELECT bp FROM BudgetPlan bp LEFT JOIN FETCH bp.items WHERE bp.quarter.id = :quarterId ORDER BY bp.month ASC")
    List<BudgetPlan> findByQuarterIdWithItems(@Param("quarterId") UUID quarterId);

    // 달력 연도 Y의 계획은 겨울학기 때문에 quarter.year가 Y-1인 분기에도 들어 있다.
    // 후보를 넓게 가져온 뒤 BudgetCalendar로 실제 달력 연월을 걸러 쓴다.
    @Query("SELECT bp FROM BudgetPlan bp JOIN FETCH bp.quarter LEFT JOIN FETCH bp.items " +
            "WHERE bp.quarter.year IN :years ORDER BY bp.month ASC")
    List<BudgetPlan> findByQuarterYearsWithItems(@Param("years") Collection<Integer> years);

    @Query("SELECT bp FROM BudgetPlan bp JOIN FETCH bp.quarter LEFT JOIN FETCH bp.items " +
            "WHERE bp.quarter.year IN :years AND bp.month = :month")
    List<BudgetPlan> findByQuarterYearsAndMonthWithItems(
            @Param("years") Collection<Integer> years,
            @Param("month") Integer month
    );
}
