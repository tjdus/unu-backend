package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ActivityParticipationRequestDto {
    private Long activityId;
}
