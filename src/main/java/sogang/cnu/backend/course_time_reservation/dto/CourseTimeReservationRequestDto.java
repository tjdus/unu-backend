package sogang.cnu.backend.course_time_reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class CourseTimeReservationRequestDto {
    private UUID activityId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
