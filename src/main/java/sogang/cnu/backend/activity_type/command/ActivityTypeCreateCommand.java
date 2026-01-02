package sogang.cnu.backend.activity_type.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Builder
public class ActivityTypeCreateCommand {
    private String name;
}

