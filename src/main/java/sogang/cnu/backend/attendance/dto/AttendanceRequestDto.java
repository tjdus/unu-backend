package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AttendanceRequestDto {
    private UUID sessionId;
    private UUID participantId;
    private String status;
}
