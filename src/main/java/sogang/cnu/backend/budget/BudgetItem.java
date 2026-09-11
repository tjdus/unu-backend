package sogang.cnu.backend.budget;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

/**
 * 예산 항목 (수입/지출 개별 항목)
 */
@Entity
@Table(
    name = "budget_items",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_budget_item_plan_category",
        columnNames = {"budget_plan_id", "category"}
    )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budget_plan_id", nullable = false)
    private BudgetPlan budgetPlan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetCategory category;

    @Column(nullable = false)
    private Long plannedAmount; // 예상 금액 (음수=지출, 양수=수입)

    private Long actualAmount;  // 실제 금액

    @Column(columnDefinition = "TEXT")
    private String note;        // 메모 (ex: "강의자: 홍길동")

    private Integer displayOrder; // 표시 순서

    public void update(Long plannedAmount, Long actualAmount, String note, Integer displayOrder) {
        this.plannedAmount = plannedAmount;
        this.actualAmount = actualAmount;
        this.note = note;
        this.displayOrder = displayOrder;
    }
}
