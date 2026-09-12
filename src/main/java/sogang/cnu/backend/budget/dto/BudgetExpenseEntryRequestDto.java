package sogang.cnu.backend.budget.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sogang.cnu.backend.budget.BudgetCategory;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BudgetExpenseEntryRequestDto {
    private UUID quarterId;
    private Integer month;
    private BudgetCategory category;
    private String label;
    private Long plannedAmount;   // 양수로 보내도 서비스에서 음수로 정규화한다
    private Long actualAmount;
    private LocalDate occurredAt;
    private String note;
}
