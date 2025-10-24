package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ActivityRequestDto {
    private String title;
    private String description;
    private String status;
    private Integer activityTypeId;
    private Long assigneeId;
    private Long quarterId;
    private LocalDate startDate;
    private LocalDate endDate;
}
