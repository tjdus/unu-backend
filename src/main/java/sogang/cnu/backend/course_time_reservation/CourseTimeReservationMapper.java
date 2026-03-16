package sogang.cnu.backend.course_time_reservation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.course_time_reservation.dto.CourseTimeReservationResponseDto;

import java.time.Duration;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface CourseTimeReservationMapper {

    @Mapping(target = "durationMinutes", expression = "java(durationMinutes(reservation.getStartAt(), reservation.getEndAt()))")
    CourseTimeReservationResponseDto toResponseDto(CourseTimeReservation reservation);

    default long durationMinutes(LocalDateTime startAt, LocalDateTime endAt) {
        return Duration.between(startAt, endAt).toMinutes();
    }
}
