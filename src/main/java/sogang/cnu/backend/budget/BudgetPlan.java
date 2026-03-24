package sogang.cnu.backend.budget;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 월별 예산 계획
 * - 분기(Quarter) + 월(month)로 구분
 * - 해당 월의 수입/지출 항목 목록을 가짐
 */
@Entity
@Table(
    name = "budget_plans",
    uniqueConstraints = @UniqueConstraint(columnNames = {"quarter_id", "month"})
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Column(nullable = false)
    private Integer month; // 1~12

    @Column(columnDefinition = "TEXT")
    private String note;

    @OneToMany(mappedBy = "budgetPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BudgetItem> items = new ArrayList<>();

    public void update(String note) {
        this.note = note;
    }
}
