package sogang.cnu.backend.course_time_reservation.command;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseTimeReservationUpdateCommand {
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
