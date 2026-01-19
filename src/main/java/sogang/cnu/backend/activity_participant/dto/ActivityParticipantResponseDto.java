package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivityParticipantResponseDto {
    private Long id;
    private ActivityResponseDto activity;
    private UserResponseDto user;
    private ActivityParticipantStatus status;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}
