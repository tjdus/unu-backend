package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivityCapacityResponseDto {
    private Integer participantLimit;
    private long participantCount;
    private boolean full;
}
