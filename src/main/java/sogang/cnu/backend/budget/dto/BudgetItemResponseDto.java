package sogang.cnu.backend.budget.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.budget.BudgetCategory;
import sogang.cnu.backend.budget.BudgetItem;

import java.util.UUID;

@Getter
@Builder
public class BudgetItemResponseDto {
    private UUID id;
    private BudgetCategory category;
    private Long plannedAmount;
    private Long actualAmount;
    private String note;
    private Integer displayOrder;

    public static BudgetItemResponseDto from(BudgetItem item) {
        return BudgetItemResponseDto.builder()
                .id(item.getId())
                .category(item.getCategory())
                .plannedAmount(item.getPlannedAmount())
                .actualAmount(item.getActualAmount())
                .note(item.getNote())
                .displayOrder(item.getDisplayOrder())
                .build();
    }
}
