package sogang.cnu.backend.activity_participant.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Getter
@Builder
public class ActivityParticipantUpdateCommand {
    private ActivityParticipantStatus status;
    private Boolean completed;
}

