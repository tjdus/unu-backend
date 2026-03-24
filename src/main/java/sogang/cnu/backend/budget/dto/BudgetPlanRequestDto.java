package sogang.cnu.backend.budget.dto;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class BudgetPlanRequestDto {
    private UUID quarterId;
    private Integer month;
    private String note;
    private List<BudgetItemRequestDto> items;
}
