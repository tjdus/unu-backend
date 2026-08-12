package sogang.cnu.backend.activity;

import org.junit.jupiter.api.Test;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity_type.ActivityType;

import static org.assertj.core.api.Assertions.assertThat;

class ActivityTest {

    @Test
    void lectureUsesFiveAsDefaultParticipantLimit() {
        Activity activity = Activity.create(ActivityCreateCommand.builder()
                .activityType(ActivityType.builder().code("LECTURE").build())
                .build());

        assertThat(activity.getParticipantLimit()).isEqualTo(5);
    }

    @Test
    void otherActivityTypesHaveNoDefaultParticipantLimit() {
        Activity activity = Activity.create(ActivityCreateCommand.builder()
                .activityType(ActivityType.builder().code("STUDY").build())
                .build());

        assertThat(activity.getParticipantLimit()).isNull();
    }

    @Test
    void explicitParticipantLimitOverridesLectureDefault() {
        Activity activity = Activity.create(ActivityCreateCommand.builder()
                .activityType(ActivityType.builder().code("LECTURE").build())
                .participantLimit(8)
                .build());

        assertThat(activity.getParticipantLimit()).isEqualTo(8);
    }
}
