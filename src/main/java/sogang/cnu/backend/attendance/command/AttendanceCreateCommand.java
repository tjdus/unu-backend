package sogang.cnu.backend.attendance.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.attendance.AttendanceStatus;

@Getter
@Builder
public class AttendanceCreateCommand {
    private ActivitySession session;
    private ActivityParticipant participant;
    private AttendanceStatus status;
}

