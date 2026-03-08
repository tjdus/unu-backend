package sogang.cnu.backend.attendance_report;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.attendance_report.dto.AttendanceReportResponseDto;

@Mapper(componentModel = "spring")
public interface AttendanceReportMapper {

    @Mapping(target = "attendanceId", source = "attendance.id")
    @Mapping(target = "sessionId", source = "attendance.session.id")
    @Mapping(target = "participantId", source = "attendance.participant.id")
    @Mapping(target = "attendanceStatus", source = "attendance.status")
    AttendanceReportResponseDto toResponseDto(AttendanceReport attendanceReport);
}
