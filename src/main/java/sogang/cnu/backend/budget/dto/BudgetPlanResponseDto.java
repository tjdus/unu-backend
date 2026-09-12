package sogang.cnu.backend.budget.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.budget.BudgetCategory;
import sogang.cnu.backend.budget.BudgetItem;
import sogang.cnu.backend.budget.BudgetPlan;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class BudgetPlanResponseDto {
    private UUID id;
    private UUID quarterId;
    private String quarterName;
    private Integer month;
    private String note;
    private List<BudgetItemResponseDto> items;

    // 자동 계산 필드
    private Long totalIncome;        // 예상 수입 합계
    private Long totalExpense;       // 예상 지출 합계 (양수로 표현)
    private Long plannedMargin;      // 예상 마진 (예상 수입 - 예상 지출)
    private Long actualIncome;       // 실제 수입 합계
    private Long actualExpense;      // 실제 지출 합계 (양수로 표현)
    private Long actualMargin;       // 실제 마진 (실제 수입 - 실제 지출)

    // 그 달에 건별 상세 내역이 있어 금액을 직접 못 고치는 카테고리 (서비스에서 채운다)
    @Setter
    @Builder.Default
    private List<BudgetCategory> entryManagedCategories = List.of();

    public static BudgetPlanResponseDto from(BudgetPlan plan) {
        List<BudgetItemResponseDto> itemDtos = plan.getItems().stream()
                .sorted((a, b) -> {
                    int orderA = a.getDisplayOrder() != null ? a.getDisplayOrder() : 999;
                    int orderB = b.getDisplayOrder() != null ? b.getDisplayOrder() : 999;
                    return Integer.compare(orderA, orderB);
                })
                .map(BudgetItemResponseDto::from)
                .collect(Collectors.toList());

        // 수입 합계 (INCOME_ 으로 시작하는 카테고리)
        long totalIncome = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("INCOME_"))
                .mapToLong(i -> i.getPlannedAmount() != null ? i.getPlannedAmount() : 0L)
                .sum();

        // 지출 합계 (EXPENSE_ 으로 시작하는 카테고리, 절댓값)
        long totalExpense = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("EXPENSE_"))
                .mapToLong(i -> i.getPlannedAmount() != null ? Math.abs(i.getPlannedAmount()) : 0L)
                .sum();

        // 실제 수입/지출
        long actualIncome = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("INCOME_") && i.getActualAmount() != null)
                .mapToLong(BudgetItem::getActualAmount)
                .sum();

        long actualExpense = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("EXPENSE_") && i.getActualAmount() != null)
                .mapToLong(i -> Math.abs(i.getActualAmount()))
                .sum();

        return BudgetPlanResponseDto.builder()
                .id(plan.getId())
                .quarterId(plan.getQuarter().getId())
                .quarterName(plan.getQuarter().getName())
                .month(plan.getMonth())
                .note(plan.getNote())
                .items(itemDtos)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .plannedMargin(totalIncome - totalExpense)
                .actualIncome(actualIncome)
                .actualExpense(actualExpense)
                .actualMargin(actualIncome - actualExpense)
                .build();
    }
}
