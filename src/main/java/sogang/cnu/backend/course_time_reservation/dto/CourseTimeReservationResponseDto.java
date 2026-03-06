package sogang.cnu.backend.course_time_reservation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

import java.util.UUID;

@Getter
@Setter
@Builder
public class CourseTimeReservationResponseDto {
    private UUID id;
    private ActivityResponseDto activity;
    private UserResponseDto user;
    private String startAt;
    private String endAt;
    private Long durationMinutes;
    private String createdAt;
}
