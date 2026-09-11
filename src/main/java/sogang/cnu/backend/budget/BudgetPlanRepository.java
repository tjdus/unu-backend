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

    // 이월금 계산용 — 전월이 다른 학기(또는 이전 연도 12월)에 속할 수 있으므로 분기가 아닌 연도+월로 찾는다
    @Query("SELECT bp FROM BudgetPlan bp LEFT JOIN FETCH bp.items " +
            "WHERE bp.quarter.year = :year AND bp.month = :month")
    List<BudgetPlan> findByYearAndMonthWithItems(
            @Param("year") Integer year,
            @Param("month") Integer month
    );
}
