package sogang.cnu.backend.activity_opening_period;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_opening_period.dto.ActivityOpeningPeriodRequestDto;
import sogang.cnu.backend.activity_opening_period.dto.ActivityOpeningPeriodResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.quarter.CurrentQuarter;
import sogang.cnu.backend.quarter.CurrentQuarterRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityOpeningPeriodService {
    private static final UUID CURRENT_QUARTER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final ActivityOpeningPeriodRepository periodRepository;
    private final CurrentQuarterRepository currentQuarterRepository;

    @Transactional(readOnly = true)
    public ActivityOpeningPeriodResponseDto getCurrent() {
        Quarter quarter = getCurrentQuarter();
        if (quarter == null) {
            return emptyResponse(null);
        }
        return periodRepository.findByQuarterId(quarter.getId())
                .map(this::toResponse)
                .orElseGet(() -> emptyResponse(quarter));
    }

    @Transactional
    public ActivityOpeningPeriodResponseDto upsertCurrent(ActivityOpeningPeriodRequestDto dto) {
        validate(dto);
        Quarter quarter = requireCurrentQuarter();
        ActivityOpeningPeriod period = periodRepository.findByQuarterId(quarter.getId())
                .orElseGet(() -> ActivityOpeningPeriod.builder()
                        .quarter(quarter)
                        .build());
        period.update(
                dto.getStartAt(),
                dto.getEndAt(),
                dto.getRevisionEndAt(),
                Boolean.TRUE.equals(dto.getEnabled())
        );
        return toResponse(periodRepository.save(period));
    }

    @Transactional(readOnly = true)
    public void requireApplicationOpen(UUID quarterId) {
        ActivityOpeningPeriod period = requirePeriodForCurrentQuarter(quarterId);
        LocalDateTime now = LocalDateTime.now();
        if (!period.isEnabled()) {
            throw new BadRequestException("현재 활동 개설 신청 접수가 중지되어 있습니다.");
        }
        if (now.isBefore(period.getStartAt())) {
            throw new BadRequestException("활동 개설 신청 기간이 아직 시작되지 않았습니다.");
        }
        if (now.isAfter(period.getEndAt())) {
            throw new BadRequestException("활동 개설 신청 기간이 마감되었습니다.");
        }
    }

    @Transactional(readOnly = true)
    public void requireRevisionOpen(UUID quarterId) {
        ActivityOpeningPeriod period = requirePeriodForCurrentQuarter(quarterId);
        if (!period.isEnabled()) {
            throw new BadRequestException("현재 활동 개설 신청 접수가 중지되어 있습니다.");
        }
        if (LocalDateTime.now().isAfter(period.getRevisionEndAt())) {
            throw new BadRequestException("활동 개설 신청 보완 제출 기간이 마감되었습니다.");
        }
    }

    private ActivityOpeningPeriod requirePeriodForCurrentQuarter(UUID quarterId) {
        Quarter currentQuarter = requireCurrentQuarter();
        if (!currentQuarter.getId().equals(quarterId)) {
            throw new BadRequestException("현재 분기의 활동만 개설 신청할 수 있습니다.");
        }
        return periodRepository.findByQuarterId(quarterId)
                .orElseThrow(() -> new BadRequestException("활동 개설 신청 기간이 아직 설정되지 않았습니다."));
    }

    private void validate(ActivityOpeningPeriodRequestDto dto) {
        if (!dto.getStartAt().isBefore(dto.getEndAt())) {
            throw new BadRequestException("신청 마감 일시는 시작 일시보다 늦어야 합니다.");
        }
        if (dto.getRevisionEndAt().isBefore(dto.getEndAt())) {
            throw new BadRequestException("보완 제출 마감은 신청 마감보다 이르게 설정할 수 없습니다.");
        }
    }

    private Quarter requireCurrentQuarter() {
        Quarter quarter = getCurrentQuarter();
        if (quarter == null) {
            throw new BadRequestException("현재 분기를 먼저 설정해주세요.");
        }
        return quarter;
    }

    private Quarter getCurrentQuarter() {
        return currentQuarterRepository.findById(CURRENT_QUARTER_ID)
                .map(CurrentQuarter::getQuarter)
                .orElse(null);
    }

    private ActivityOpeningPeriodResponseDto toResponse(ActivityOpeningPeriod period) {
        LocalDateTime now = LocalDateTime.now();
        ActivityOpeningPeriodStatus status = statusOf(period, now);
        return ActivityOpeningPeriodResponseDto.builder()
                .id(period.getId())
                .quarter(toQuarterResponse(period.getQuarter()))
                .startAt(period.getStartAt())
                .endAt(period.getEndAt())
                .revisionEndAt(period.getRevisionEndAt())
                .enabled(period.isEnabled())
                .status(status)
                .canApply(status == ActivityOpeningPeriodStatus.OPEN)
                .canRevise(period.isEnabled() && !now.isAfter(period.getRevisionEndAt()))
                .build();
    }

    private ActivityOpeningPeriodStatus statusOf(ActivityOpeningPeriod period, LocalDateTime now) {
        if (!period.isEnabled()) return ActivityOpeningPeriodStatus.DISABLED;
        if (now.isBefore(period.getStartAt())) return ActivityOpeningPeriodStatus.UPCOMING;
        if (now.isAfter(period.getEndAt())) return ActivityOpeningPeriodStatus.CLOSED;
        return ActivityOpeningPeriodStatus.OPEN;
    }

    private ActivityOpeningPeriodResponseDto emptyResponse(Quarter quarter) {
        return ActivityOpeningPeriodResponseDto.builder()
                .quarter(quarter == null ? null : toQuarterResponse(quarter))
                .enabled(false)
                .status(ActivityOpeningPeriodStatus.NOT_CONFIGURED)
                .canApply(false)
                .canRevise(false)
                .build();
    }

    private QuarterResponseDto toQuarterResponse(Quarter quarter) {
        return QuarterResponseDto.builder()
                .id(quarter.getId())
                .name(quarter.getName())
                .year(quarter.getYear())
                .season(quarter.getSeason().name())
                .startDate(quarter.getStartDate())
                .endDate(quarter.getEndDate())
                .build();
    }
}
