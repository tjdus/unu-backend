package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AttendanceBulkRequestDto {
    private UUID sessionId;
    private List<UUID> presentParticipantIds;
    private List<UUID> absentParticipantIds;
    private List<UUID> excusedParticipantIds;
}
