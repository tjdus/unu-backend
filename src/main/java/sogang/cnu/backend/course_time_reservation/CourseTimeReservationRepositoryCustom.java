package sogang.cnu.backend.course_time_reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CourseTimeReservationRepositoryCustom {

    boolean existsOverlapping(UUID userId, LocalDateTime newStart, LocalDateTime newEnd, UUID excludeId);

    long sumMinutesOnKstDate(UUID userId, LocalDate kstDate, UUID excludeId);

    List<CourseTimeReservation> findByUserAndFilters(UUID userId, UUID activityId, LocalDate kstDate);

    List<CourseTimeReservation> findByActivityAndFilters(UUID activityId, UUID userId, LocalDate kstDate);
}
