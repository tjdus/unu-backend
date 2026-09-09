package sogang.cnu.backend.course_time_reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CourseTimeReservationSlotResponseDto {
    private UUID id;
    private String startAt;
    private String endAt;
    private Long durationMinutes;
}
