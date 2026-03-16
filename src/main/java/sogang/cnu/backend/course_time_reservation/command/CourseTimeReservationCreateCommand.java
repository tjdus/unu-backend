package sogang.cnu.backend.course_time_reservation.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.user.User;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseTimeReservationCreateCommand {
    private Activity activity;
    private User user;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
