package sogang.cnu.backend.budget.dto;

import java.util.List;

/** 엑셀 업로드 미리보기/적용 결과. 값이 실제로 바뀌는 칸만 changes에 담는다. */
public record BudgetImportResultDto(
        int year,
        List<MonthResult> months,
        List<String> warnings,
        List<String> errors
) {
    public enum Status { CREATE, UPDATE, SKIP, ERROR }

    public record MonthResult(
            int month,
            Status status,
            String quarterName,
            List<Change> changes
    ) {}

    public record Change(
            String categoryLabel,
            String field,   // "예상" | "실제"
            long before,
            long after
    ) {}
}
