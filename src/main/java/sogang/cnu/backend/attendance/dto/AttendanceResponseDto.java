package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttendanceResponseDto {
    private Long sessionId;
    private Long participantId;
    private String status;
}
