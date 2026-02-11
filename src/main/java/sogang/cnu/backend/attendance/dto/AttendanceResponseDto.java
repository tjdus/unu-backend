package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;

@Getter
@Setter
@Builder
public class AttendanceResponseDto {
    private ActivitySessionResponseDto session;
    private ActivityParticipantResponseDto participant;
    private String status;
}
