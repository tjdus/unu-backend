package sogang.cnu.backend.activity_participant;

import org.junit.jupiter.api.Test;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityParticipantTest {

    @Test
    void appliedParticipantIsConfirmedAtActivityStart() {
        LocalDate startDate = LocalDate.of(2026, 9, 1);
        Activity activity = Activity.builder()
                .startDate(startDate)
                .build();
        ActivityParticipant participant = ActivityParticipant.create(
                ActivityParticipantCreateCommand.builder()
                        .activity(activity)
                        .status(ActivityParticipantStatus.APPLIED)
                        .build()
        );

        participant.confirmOnActivityStart();

        assertThat(participant.getStatus()).isEqualTo(ActivityParticipantStatus.APPROVED);
        assertThat(participant.getJoinedAt()).isEqualTo(startDate.atStartOfDay());
    }

    @Test
    void movingBackToAppliedClearsPreviousConfirmationTime() {
        ActivityParticipant participant = ActivityParticipant.builder()
                .status(ActivityParticipantStatus.APPROVED)
                .joinedAt(LocalDate.of(2026, 8, 1).atStartOfDay())
                .build();

        participant.updateStatus(ActivityParticipantStatus.APPLIED);

        assertThat(participant.getStatus()).isEqualTo(ActivityParticipantStatus.APPLIED);
        assertThat(participant.getJoinedAt()).isNull();
    }
}
