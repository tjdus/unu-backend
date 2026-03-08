package sogang.cnu.backend.attendance_report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AttendanceReportResponseDto {
    private UUID id;
    private String title;
    private String content;
    private UUID attendanceId;
    private UUID sessionId;
    private UUID participantId;
    private String attendanceStatus;
    private LocalDateTime createdAt;
}
