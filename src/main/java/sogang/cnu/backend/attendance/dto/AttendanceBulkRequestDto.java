package sogang.cnu.backend.attendance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class AttendanceBulkRequestDto {
    @NotNull(message = "활동 회차를 선택해주세요.")
    private UUID sessionId;
    private List<UUID> presentParticipantIds;
    private List<UUID> absentParticipantIds;
    private List<UUID> excusedParticipantIds;
}
