package sogang.cnu.backend.lecture_room_schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

public interface LectureRoomScheduleRepository extends JpaRepository<LectureRoomSchedule, UUID> {
    List<LectureRoomSchedule> findByQuarterId(UUID quarterId);
    List<LectureRoomSchedule> findByQuarterIdAndDayOfWeek(UUID quarterId, DayOfWeek dayOfWeek);
    boolean existsByQuarterIdAndDayOfWeekAndTimeSlotAndUserId(UUID quarterId, DayOfWeek dayOfWeek, java.time.LocalTime timeSlot, UUID userId);
}
