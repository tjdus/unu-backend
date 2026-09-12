package sogang.cnu.backend.budget.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.budget.BudgetCategory;
import sogang.cnu.backend.budget.BudgetExpenseEntry;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class BudgetExpenseEntryDto {
    private UUID id;
    private UUID quarterId;
    private Integer month;
    private BudgetCategory category;
    private String label;
    private Long plannedAmount;
    private Long actualAmount;
    private LocalDate occurredAt;
    private String note;

    public static BudgetExpenseEntryDto from(BudgetExpenseEntry entry) {
        return BudgetExpenseEntryDto.builder()
                .id(entry.getId())
                .quarterId(entry.getQuarter().getId())
                .month(entry.getMonth())
                .category(entry.getCategory())
                .label(entry.getLabel())
                .plannedAmount(entry.getPlannedAmount())
                .actualAmount(entry.getActualAmount())
                .occurredAt(entry.getOccurredAt())
                .note(entry.getNote())
                .build();
    }
}
