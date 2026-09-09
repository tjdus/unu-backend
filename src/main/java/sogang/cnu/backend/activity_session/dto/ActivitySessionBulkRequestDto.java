package sogang.cnu.backend.activity_session.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class ActivitySessionBulkRequestDto {
    @NotNull(message = "활동을 선택해주세요.")
    private UUID activityId;

    @NotNull(message = "일정 시작일을 입력해주세요.")
    private LocalDate startDate;

    @NotNull(message = "일정 종료일을 입력해주세요.")
    private LocalDate endDate;

    @NotEmpty(message = "반복 요일을 하나 이상 선택해주세요.")
    private Set<DayOfWeek> weekdays = new LinkedHashSet<>();

    @Min(value = 1, message = "반복 주기는 1주 이상이어야 합니다.")
    @Max(value = 4, message = "반복 주기는 4주 이하여야 합니다.")
    @NotNull(message = "반복 주기를 선택해주세요.")
    private Integer intervalWeeks = 1;

    private Set<LocalDate> excludedDates = new LinkedHashSet<>();
    private String description;
}
