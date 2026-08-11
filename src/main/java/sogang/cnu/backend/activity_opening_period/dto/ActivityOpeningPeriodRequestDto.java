package sogang.cnu.backend.activity_opening_period.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ActivityOpeningPeriodRequestDto {
    @NotNull(message = "신청 시작 일시를 입력해주세요.")
    private LocalDateTime startAt;

    @NotNull(message = "신청 마감 일시를 입력해주세요.")
    private LocalDateTime endAt;

    @NotNull(message = "보완 제출 마감 일시를 입력해주세요.")
    private LocalDateTime revisionEndAt;

    @NotNull(message = "신청 접수 여부를 선택해주세요.")
    private Boolean enabled;
}
