package sogang.cnu.backend.lecture_room_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LectureRoomScheduleTimeSlotBackfill implements ApplicationRunner {
    private static final Map<LocalTime, LocalTime> LEGACY_TIME_SLOTS = Map.of(
            LocalTime.of(10, 30), LocalTime.of(10, 15),
            LocalTime.of(12, 0), LocalTime.of(11, 45),
            LocalTime.of(13, 30), LocalTime.of(13, 15),
            LocalTime.of(15, 0), LocalTime.of(14, 45),
            LocalTime.of(16, 30), LocalTime.of(16, 15),
            LocalTime.of(18, 0), LocalTime.of(17, 45),
            LocalTime.of(19, 30), LocalTime.of(19, 15)
    );

    private final LectureRoomScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        scheduleRepository.findAll().forEach(schedule -> {
            LocalTime newTimeSlot = LEGACY_TIME_SLOTS.get(schedule.getTimeSlot());
            if (newTimeSlot == null) {
                return;
            }

            boolean duplicate = scheduleRepository.existsByQuarterIdAndDayOfWeekAndTimeSlotAndUserId(
                    schedule.getQuarter().getId(),
                    schedule.getDayOfWeek(),
                    newTimeSlot,
                    schedule.getUser().getId()
            );
            if (duplicate) {
                scheduleRepository.delete(schedule);
                return;
            }

            schedule.changeTimeSlot(newTimeSlot);
        });
    }
}
