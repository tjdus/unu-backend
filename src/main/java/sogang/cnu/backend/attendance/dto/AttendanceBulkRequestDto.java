package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class AttendanceBulkRequestDto {
    private Long sessionId;
    private List<Long> presentParticipantIds;
    private List<Long> absentParticipantIds;
    private List<Long> excusedParticipantIds;
}
