package sogang.cnu.backend.activity_participant.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.user.User;


@Getter
@Builder
public class ActivityParticipantCreateCommand {
    private Activity activity;
    private User user;
    private ActivityParticipantStatus status;
}

