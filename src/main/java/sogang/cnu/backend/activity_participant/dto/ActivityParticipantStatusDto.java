package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityParticipantStatusDto {
    private UUID id;
    private ActivityParticipantStatus status;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}
