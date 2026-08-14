package sogang.cnu.backend.menu_notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.menu_notification.dto.MenuNotificationSummaryDto;
import sogang.cnu.backend.quarter.CurrentQuarter;
import sogang.cnu.backend.quarter.CurrentQuarterRepository;
import sogang.cnu.backend.recruitment.RecruitmentRepository;
import sogang.cnu.backend.recruitment.RecruitmentType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuNotificationService {
    private static final UUID CURRENT_QUARTER_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final int NEW_CARD_DAYS = 7;

    private final ActivityRepository activityRepository;
    private final RecruitmentRepository recruitmentRepository;
    private final CurrentQuarterRepository currentQuarterRepository;
    private final ActivityCardReadRepository activityCardReadRepository;
    private final RecruitmentCardReadRepository recruitmentCardReadRepository;

    @Transactional
    public MenuNotificationSummaryDto getSummary(UUID userId) {
        var currentQuarter = currentQuarterRepository.findById(CURRENT_QUARTER_ID)
                .map(CurrentQuarter::getQuarter);
        LocalDateTime cutoff = LocalDateTime.now().minusDays(NEW_CARD_DAYS);
        List<UUID> newActivityIds = currentQuarter
                .map(quarter -> activityRepository.findUnreadRecentListedActivityIds(
                        quarter.getId(), userId, cutoff))
                .orElseGet(List::of);
        List<UUID> newOperationRecruitmentIds = recruitmentRepository.findUnreadRecentIdsByType(
                RecruitmentType.INTERNAL_OPERATION,
                userId,
                cutoff
        );
        return MenuNotificationSummaryDto.builder()
                .activityCount(newActivityIds.size())
                .operationRecruitmentCount(newOperationRecruitmentIds.size())
                .newActivityIds(newActivityIds)
                .newOperationRecruitmentIds(newOperationRecruitmentIds)
                .build();
    }

    @Transactional
    public void markActivityCardRead(UUID userId, UUID activityId) {
        var activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
        if (activity.getCreatedAt() == null
                || activity.getCreatedAt().isBefore(LocalDateTime.now().minusDays(NEW_CARD_DAYS))) {
            return;
        }
        activityCardReadRepository.insertIgnore(
                UUID.randomUUID(), activityId, userId, LocalDateTime.now());
    }

    @Transactional
    public void markOperationRecruitmentCardRead(UUID userId, UUID recruitmentId) {
        var recruitment = recruitmentRepository.findById(recruitmentId)
                .filter(item -> item.getType() == RecruitmentType.INTERNAL_OPERATION)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
        if (recruitment.getCreatedAt() == null
                || recruitment.getCreatedAt().isBefore(LocalDateTime.now().minusDays(NEW_CARD_DAYS))) {
            return;
        }
        recruitmentCardReadRepository.insertIgnore(
                UUID.randomUUID(), recruitmentId, userId, LocalDateTime.now());
    }

}
