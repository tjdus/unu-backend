package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ActivityParticipantSummaryDto {
    private UUID id;
    private String name;
}
