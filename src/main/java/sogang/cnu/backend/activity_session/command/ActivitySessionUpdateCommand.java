package sogang.cnu.backend.activity_session.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Builder
public class ActivitySessionUpdateCommand {
    private Integer sessionNumber;
    private LocalDate date;
    private String description;
}

