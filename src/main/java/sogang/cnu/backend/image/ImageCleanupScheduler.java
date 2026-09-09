package sogang.cnu.backend.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupScheduler {

    private final ImageService imageService;

    /** Runs every hour — deletes TEMP images uploaded more than 1 hour ago. */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupTempImages() {
        log.info("Starting scheduled TEMP image cleanup");
        imageService.cleanupTempImages();
    }
}
