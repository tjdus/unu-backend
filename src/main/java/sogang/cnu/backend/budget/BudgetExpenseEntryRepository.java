package sogang.cnu.backend.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface BudgetExpenseEntryRepository extends JpaRepository<BudgetExpenseEntry, UUID> {

    @Query("SELECT e FROM BudgetExpenseEntry e " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month AND e.category = :category " +
            "ORDER BY e.occurredAt ASC NULLS LAST, e.createdAt ASC")
    List<BudgetExpenseEntry> findDetail(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month,
            @Param("category") BudgetCategory category
    );

    @Query("SELECT COALESCE(SUM(e.plannedAmount), 0) FROM BudgetExpenseEntry e " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month AND e.category = :category")
    long sumPlanned(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month,
            @Param("category") BudgetCategory category
    );

    @Query("SELECT COALESCE(SUM(e.actualAmount), 0) FROM BudgetExpenseEntry e " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month AND e.category = :category")
    long sumActual(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month,
            @Param("category") BudgetCategory category
    );

    /** 그 달에 건별 내역이 있는 카테고리 — 있으면 월 금액을 내역 합계로 계산한다(직접 수정 불가) */
    @Query("SELECT DISTINCT e.category FROM BudgetExpenseEntry e " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month")
    List<BudgetCategory> findCategoriesWithEntries(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month
    );

    /** 엑셀 내보내기용 — 겨울학기 때문에 두 해의 분기를 후보로 받아 서비스에서 달력 연도로 거른다 */
    @Query("SELECT e FROM BudgetExpenseEntry e JOIN FETCH e.quarter q " +
            "WHERE q.year IN :years " +
            "ORDER BY e.month ASC, e.occurredAt ASC NULLS LAST, e.createdAt ASC")
    List<BudgetExpenseEntry> findDetailByQuarterYears(@Param("years") Collection<Integer> years);
}
