package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityRequestDto {
    private String title;
    private String description;
    private String status;
    private UUID activityTypeId;
    private UUID assigneeId;
    private UUID quarterId;
    private LocalDate startDate;
    private LocalDate endDate;
}
