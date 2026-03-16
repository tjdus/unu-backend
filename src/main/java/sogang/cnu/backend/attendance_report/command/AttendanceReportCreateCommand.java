package sogang.cnu.backend.attendance_report.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.attendance.Attendance;

@Getter
@Builder
public class AttendanceReportCreateCommand {
    private Attendance attendance;
    private String title;
    private String content;
}
