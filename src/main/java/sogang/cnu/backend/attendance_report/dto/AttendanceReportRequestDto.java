package sogang.cnu.backend.attendance_report.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AttendanceReportRequestDto {
    private UUID sessionId;
    private UUID participantId;
    private String title;
    private String content;
}
