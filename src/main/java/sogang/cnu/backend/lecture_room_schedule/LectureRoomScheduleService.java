package sogang.cnu.backend.lecture_room_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.lecture_room_schedule.command.LectureRoomScheduleCreateCommand;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleRequestDto;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleResponseDto;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureRoomScheduleService {

    private static final Set<DayOfWeek> WEEKDAYS = Set.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
    );

    private final LectureRoomScheduleRepository lectureRoomScheduleRepository;
    private final LectureRoomScheduleMapper lectureRoomScheduleMapper;
    private final QuarterRepository quarterRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<LectureRoomScheduleResponseDto> getByQuarter(UUID quarterId) {
        return lectureRoomScheduleRepository.findByQuarterId(quarterId).stream()
                .map(lectureRoomScheduleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureRoomScheduleResponseDto> getByQuarterAndDay(UUID quarterId, String dayOfWeek) {
        DayOfWeek day = parseDayOfWeek(dayOfWeek);
        return lectureRoomScheduleRepository.findByQuarterIdAndDayOfWeek(quarterId, day).stream()
                .map(lectureRoomScheduleMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LectureRoomScheduleResponseDto create(LectureRoomScheduleRequestDto dto) {
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
        lectureRoomScheduleRepository.delete(schedule);
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
        if (timeSlot.getMinute() % 15 != 0 || timeSlot.getSecond() != 0 || timeSlot.getNano() != 0) {
            throw new BadRequestException("시간은 15분 단위로만 등록 가능합니다.");
        }
    }
}
