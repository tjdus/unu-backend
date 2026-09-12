package sogang.cnu.backend.budget;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 지출 건별 상세 내역 (인강 구매비, 엠티, 개총/종총, 관리자실 간식비, 기타).
 * 총무 시트 '수입지출 총액' 탭의 구역별 줄에 해당한다.
 * 해당 카테고리의 BudgetItem 예상·실제 금액은 이 테이블의 합계를 반영하는 파생 캐시로 취급한다
 * (보증금 원장 StudyDepositLedgerEntry와 같은 구조).
 */
@Entity
@Table(
        name = "budget_expense_entries",
        indexes = @Index(
                name = "idx_budget_expense_entry_quarter_month_category",
                columnList = "quarter_id,month,category"
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetExpenseEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Column(nullable = false)
    private Integer month; // 1~12

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetCategory category; // BudgetCategory.DETAIL_MANAGED_CATEGORIES 전용

    @Column(nullable = false, length = 100)
    private String label; // 항목명 (예: dreamhack, mt 장소 1차 결제)

    @Column(name = "planned_amount", nullable = false)
    private Long plannedAmount; // 지출이므로 음수 (BudgetItem 부호 규칙과 동일)

    @Column(name = "actual_amount", nullable = false)
    private Long actualAmount;

    @Column(name = "occurred_at")
    private LocalDate occurredAt; // 거래 일자 (계획만 잡아둔 줄은 비어 있을 수 있다)

    @Column(length = 200)
    private String note;

    public void update(String label, Long plannedAmount, Long actualAmount,
                       LocalDate occurredAt, String note) {
        this.label = label;
        this.plannedAmount = plannedAmount;
        this.actualAmount = actualAmount;
        this.occurredAt = occurredAt;
        this.note = note;
    }
}
