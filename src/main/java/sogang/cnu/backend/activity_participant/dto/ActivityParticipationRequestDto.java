package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityParticipationRequestDto {
    private UUID activityId;
}
