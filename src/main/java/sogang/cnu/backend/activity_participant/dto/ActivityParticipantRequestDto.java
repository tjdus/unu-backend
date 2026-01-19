package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ActivityParticipantRequestDto {
    private Long activityId;
    private Long userId;
    private String status;
}
