package sogang.cnu.backend.activity.command;

import lombok.*;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Builder
public class ActivityCreateCommand {
    private String title;
    private String description;
    private ActivityStatus status;
    private ActivityType activityType;
    private User assignee;
    private Quarter quarter;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;
    private Activity parentActivity;
    private Boolean listed;
    private Integer depositAmount;
    private Integer participantLimit;
    private String recruitmentPositions;
    private String discordUrl;
    private String operationPlan;
    private String instructorCareer;
}
