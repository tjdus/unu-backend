package sogang.cnu.backend.attendance;

import org.mapstruct.Mapper;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    AttendanceResponseDto toResponseDto(Attendance attendance);
}


