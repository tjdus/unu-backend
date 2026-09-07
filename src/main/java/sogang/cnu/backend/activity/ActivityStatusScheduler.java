package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusScheduler {
    private final ActivityService activityService;

    @EventListener(ApplicationReadyEvent.class)
    public void synchronizeAfterStartup() {
        synchronizeStatuses();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void synchronizeAtStartOfDay() {
        synchronizeStatuses();
    }

    private void synchronizeStatuses() {
        int changed = activityService.synchronizeScheduledStatuses();
        if (changed > 0) {
            log.info("Synchronized {} activity statuses from activity schedules", changed);
        }
    }
}
