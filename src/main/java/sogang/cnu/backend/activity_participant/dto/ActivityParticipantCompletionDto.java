package sogang.cnu.backend.activity_participant.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActivityParticipantCompletionDto {
    @NotNull(message = "수료 여부를 지정해주세요.")
    private Boolean completed;
}
