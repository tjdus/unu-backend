package sogang.cnu.backend.applicant_notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.applicant_notification.dto.ActivityApplicantCountDto;
import sogang.cnu.backend.applicant_notification.dto.ApplicantNotificationSummaryDto;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicantNotificationService {

    private static final LocalDateTime EPOCH_FALLBACK = LocalDateTime.of(2000, 1, 1, 0, 0);

    private final ManagerApplicantCheckpointRepository checkpointRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public ApplicantNotificationSummaryDto getSummary(UUID userId) {
        return buildSummary(userId, currentCheckpoint(userId));
    }

    @Transactional
    public ApplicantNotificationSummaryDto checkAndGetSummary(UUID userId) {
        LocalDateTime since = currentCheckpoint(userId);
        ApplicantNotificationSummaryDto summary = buildSummary(userId, since);
        checkpointRepository.upsertLastCheckedAt(UUID.randomUUID(), userId, LocalDateTime.now());
        return summary;
    }

    private LocalDateTime currentCheckpoint(UUID userId) {
        return checkpointRepository.findByUserId(userId)
                .map(ManagerApplicantCheckpoint::getLastCheckedAt)
                .orElse(EPOCH_FALLBACK);
    }

    private ApplicantNotificationSummaryDto buildSummary(UUID userId, LocalDateTime since) {
        List<ActivityParticipant> newApplicants = SecurityUtils.isManagerOrAdmin()
                ? activityParticipantRepository.findNewApplicants(ActivityParticipantStatus.APPLIED, since)
                : findScopedToAssignee(userId, since);

        Map<UUID, List<ActivityParticipant>> byActivity = newApplicants.stream()
                .collect(Collectors.groupingBy(ap -> ap.getActivity().getId()));

        List<ActivityApplicantCountDto> activities = byActivity.entrySet().stream()
                .map(entry -> ActivityApplicantCountDto.builder()
                        .activityId(entry.getKey())
                        .activityTitle(entry.getValue().get(0).getActivity().getTitle())
                        .newApplicantCount(entry.getValue().size())
                        .build())
                .toList();

        return ApplicantNotificationSummaryDto.builder()
                .totalCount(newApplicants.size())
                .sinceCheckpoint(since)
                .activities(activities)
                .build();
    }

    private List<ActivityParticipant> findScopedToAssignee(UUID userId, LocalDateTime since) {
        List<UUID> activityIds = activityRepository.findByAssigneeId(userId).stream()
                .map(Activity::getId)
                .toList();
        if (activityIds.isEmpty()) {
            return List.of();
        }
        return activityParticipantRepository.findNewApplicantsForActivities(
                ActivityParticipantStatus.APPLIED, since, activityIds);
    }
}
