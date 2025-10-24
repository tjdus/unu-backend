package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActivitySearchQuery {
    private String title;
    private String status;
    private Integer activityTypeId;
    private Long quarterId;
}
