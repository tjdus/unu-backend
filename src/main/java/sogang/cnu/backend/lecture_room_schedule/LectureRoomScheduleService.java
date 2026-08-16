package sogang.cnu.backend.lecture_room_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.lecture_room_schedule.command.LectureRoomScheduleCreateCommand;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleRequestDto;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleResponseDto;
import sogang.cnu.backend.quarter.CurrentQuarterService;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureRoomScheduleService {

    private static final Set<LocalTime> TIME_SLOTS = Set.of(
            LocalTime.of(9, 0),
            LocalTime.of(10, 15),
            LocalTime.of(11, 45),
            LocalTime.of(13, 15),
            LocalTime.of(14, 45),
            LocalTime.of(16, 15),
            LocalTime.of(17, 45),
            LocalTime.of(19, 15)
    );

    private static final Set<DayOfWeek> WEEKDAYS = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );

    private final LectureRoomScheduleRepository lectureRoomScheduleRepository;
    private final LectureRoomScheduleMapper lectureRoomScheduleMapper;
    private final QuarterRepository quarterRepository;
    private final UserRepository userRepository;
    private final CurrentQuarterService currentQuarterService;

    @Transactional(readOnly = true)
    public List<LectureRoomScheduleResponseDto> getByQuarter(UUID quarterId) {
        requireAllowedQuarter(quarterId);
        return lectureRoomScheduleRepository.findByQuarterId(quarterId).stream()
                .map(lectureRoomScheduleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureRoomScheduleResponseDto> getByQuarterAndDay(UUID quarterId, String dayOfWeek) {
        requireAllowedQuarter(quarterId);
        DayOfWeek day = parseDayOfWeek(dayOfWeek);
        return lectureRoomScheduleRepository.findByQuarterIdAndDayOfWeek(quarterId, day).stream()
                .map(lectureRoomScheduleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LectureRoomScheduleResponseDto create(LectureRoomScheduleRequestDto dto) {
        requireAllowedQuarter(dto.getQuarterId());
        DayOfWeek dayOfWeek = parseDayOfWeek(dto.getDayOfWeek());
        validateWeekday(dayOfWeek);
        validateTimeSlot(dto.getTimeSlot());

        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new NotFoundException("Quarter not found"));
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (lectureRoomScheduleRepository.existsByQuarterIdAndDayOfWeekAndTimeSlotAndUserId(
                dto.getQuarterId(), dayOfWeek, dto.getTimeSlot(), dto.getUserId())) {
            throw new BadRequestException("이미 동일한 시간대에 해당 사용자가 배정되어 있습니다.");
        }

        LectureRoomSchedule schedule = LectureRoomSchedule.create(
                LectureRoomScheduleCreateCommand.builder()
                        .quarter(quarter)
                        .dayOfWeek(dayOfWeek)
                        .timeSlot(dto.getTimeSlot())
                        .user(user)
                        .build()
        );

        return lectureRoomScheduleMapper.toResponseDto(lectureRoomScheduleRepository.save(schedule));
    }

    @Transactional
    public LectureRoomScheduleResponseDto createForMe(UUID currentUserId, LectureRoomScheduleRequestDto dto) {
        dto.setUserId(currentUserId);
        return create(dto);
    }

    @Transactional
    public void delete(UUID id) {
        LectureRoomSchedule schedule = lectureRoomScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("LectureRoomSchedule not found"));

        boolean isOwner = schedule.getUser() != null
                && schedule.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        boolean canManageAll = SecurityUtils.hasAnyRole("ADMIN", "MANAGER");
        if (!isOwner && !canManageAll) {
            throw new ForbiddenException("본인의 학회실 관리 시간만 삭제할 수 있습니다.");
        }
        // 조회/생성과 일관되게, 운영진이 아닌 학회실 관리자는 현재 분기 일정만 삭제할 수 있다.
        requireAllowedQuarter(schedule.getQuarter().getId());

        lectureRoomScheduleRepository.delete(schedule);
    }

    /**
     * 운영진(ADMIN/MANAGER)은 모든 분기를 허용한다.
     * 그 외(학회실 관리자)는 현재 분기만 조회·관리할 수 있으며, 다른 분기 요청은 403으로 막는다.
     */
    private void requireAllowedQuarter(UUID quarterId) {
        if (SecurityUtils.hasAnyRole("ADMIN", "MANAGER")) return;
        UUID currentQuarterId = currentQuarterService.getCurrentQuarterId();
        if (currentQuarterId == null || !currentQuarterId.equals(quarterId)) {
            throw new ForbiddenException("학회실 관리자는 현재 분기만 조회·관리할 수 있습니다.");
        }
    }

    private DayOfWeek parseDayOfWeek(String dayOfWeek) {
        try {
            return DayOfWeek.valueOf(dayOfWeek.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("유효하지 않은 요일입니다: " + dayOfWeek);
        }
    }

    private void validateWeekday(DayOfWeek dayOfWeek) {
        if (!WEEKDAYS.contains(dayOfWeek)) {
            throw new BadRequestException("월요일부터 금요일까지만 등록 가능합니다.");
        }
    }

    private void validateTimeSlot(LocalTime timeSlot) {
        if (!TIME_SLOTS.contains(timeSlot)) {
            throw new BadRequestException("등록 가능한 학회실 관리 시간이 아닙니다.");
        }
    }
}
