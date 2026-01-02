package sogang.cnu.backend.activity.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Builder
public class ActivityUpdateCommand {
    private String title;
    private String description;
    private ActivityStatus status;
    private ActivityType activityType;
    private User assignee;
    private Quarter quarter;
    private LocalDate startDate;
    private LocalDate endDate;
}

