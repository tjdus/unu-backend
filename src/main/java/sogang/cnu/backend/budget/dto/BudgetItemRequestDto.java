package sogang.cnu.backend.budget.dto;

import lombok.Getter;
import sogang.cnu.backend.budget.BudgetCategory;

@Getter
public class BudgetItemRequestDto {
    private BudgetCategory category;
    private Long plannedAmount;
    private Long actualAmount;
    private String note;
    private Integer displayOrder;
}
