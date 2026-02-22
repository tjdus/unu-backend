package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ActivitySearchQuery {
    private String title;
    private String status;
    private UUID activityTypeId;
    private UUID quarterId;
}
