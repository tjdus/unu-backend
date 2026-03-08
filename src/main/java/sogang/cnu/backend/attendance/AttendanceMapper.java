package sogang.cnu.backend.attendance;

import org.mapstruct.Mapper;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface AttendanceMapper {
    AttendanceResponseDto toResponseDto(Attendance attendance);
}


