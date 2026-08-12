package sogang.cnu.backend.activity_participant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityParticipantStatusScheduler {
    private final ActivityParticipantService activityParticipantService;

    @EventListener(ApplicationReadyEvent.class)
    public void confirmAfterStartup() {
        confirmReadyParticipants();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void confirmAtStartOfDay() {
        confirmReadyParticipants();
    }

    private void confirmReadyParticipants() {
        int confirmed = activityParticipantService
                .confirmParticipantsForStartedActivities(LocalDate.now());
        if (confirmed > 0) {
            log.info("Confirmed {} activity participation applications", confirmed);
        }
    }
}
