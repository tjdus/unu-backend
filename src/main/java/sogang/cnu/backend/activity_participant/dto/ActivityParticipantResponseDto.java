package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.util.UUID;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivityParticipantResponseDto {
    private UUID id;
    private ActivityResponseDto activity;
    private UserResponseDto user;
    private ActivityParticipantStatus status;
    private Boolean completed;
    private String completedAt;
    private String joinedAt;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
}
