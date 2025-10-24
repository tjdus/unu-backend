package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ActivityMappingDto {
    private String title;
    private String description;
    private String status;
    private ActivityType activityType;
    private User assignee;
    private Quarter quarter;
    private LocalDate startDate;
    private LocalDate endDate;
}
