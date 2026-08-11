package sogang.cnu.backend.activity_opening_period.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity_opening_period.ActivityOpeningPeriodStatus;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ActivityOpeningPeriodResponseDto {
    private UUID id;
    private QuarterResponseDto quarter;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime revisionEndAt;
    private boolean enabled;
    private ActivityOpeningPeriodStatus status;
    private boolean canApply;
    private boolean canRevise;
}
