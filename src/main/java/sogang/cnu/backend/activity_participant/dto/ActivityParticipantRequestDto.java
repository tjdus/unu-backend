package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityParticipantRequestDto {
    private UUID activityId;
    private UUID userId;
    private String status;
}
