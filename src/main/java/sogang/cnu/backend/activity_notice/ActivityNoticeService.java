package sogang.cnu.backend.activity_notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityAccessGuard;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeRequestDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeResponseDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeUnreadCountDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeUnreadSummaryDto;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityNoticeService {

    private static final String VIEW_DENIED = "해당 활동의 공지를 볼 권한이 없습니다.";
    private static final String MANAGE_DENIED = "해당 활동의 공지를 관리할 권한이 없습니다.";

    private final ActivityNoticeRepository activityNoticeRepository;
    private final ActivityRepository activityRepository;
    private final ActivityAccessGuard activityAccessGuard;
    private final ActivityNoticeReadRepository activityNoticeReadRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ActivityNoticeResponseDto> getByActivityId(UUID userId, UUID activityId) {
        Activity activity = findActivityOrThrow(activityId);
        activityAccessGuard.requireView(activity, VIEW_DENIED);
        Set<UUID> readNoticeIds = new HashSet<>(
                activityNoticeReadRepository.findReadNoticeIds(userId, activityId)
        );

        return activityNoticeRepository.findAllByActivityIdOrderByCreatedAtDesc(activityId).stream()
                .map(notice -> toResponseDto(notice, readNoticeIds.contains(notice.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityNoticeUnreadSummaryDto getUnreadSummary(UUID userId) {
        List<ActivityNoticeUnreadCountDto> activities = activityNoticeReadRepository
                .findUnreadCounts(userId, ActivityParticipantStatus.APPROVED)
                .stream()
                .map(row -> ActivityNoticeUnreadCountDto.builder()
                        .activityId((UUID) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .toList();
        return ActivityNoticeUnreadSummaryDto.builder()
                .totalCount(activities.stream().mapToLong(ActivityNoticeUnreadCountDto::getCount).sum())
                .activities(activities)
                .build();
    }

    @Transactional
    public ActivityNoticeResponseDto create(UUID userId, ActivityNoticeRequestDto request) {
        Activity activity = findActivityOrThrow(request.getActivityId());
        activityAccessGuard.requireManage(activity, MANAGE_DENIED);

        ActivityNotice notice = ActivityNotice.builder()
                .title(request.getTitle().trim())
                .content(normalizeContent(request.getContent()))
                .activity(activity)
                .build();
        ActivityNotice savedNotice = activityNoticeRepository.save(notice);
        saveReadReceipt(userId, savedNotice);
        return toResponseDto(savedNotice, true);
    }

    @Transactional
    public ActivityNoticeResponseDto update(UUID userId, UUID id, ActivityNoticeRequestDto request) {
        ActivityNotice notice = findOrThrow(id);
        activityAccessGuard.requireManage(notice.getActivity(), MANAGE_DENIED);

        notice.update(request.getTitle().trim(), normalizeContent(request.getContent()));
        activityNoticeReadRepository.deleteByNoticeId(id);
        saveReadReceipt(userId, notice);
        return toResponseDto(notice, true);
    }

    @Transactional
    public void markRead(UUID userId, UUID noticeId) {
        ActivityNotice notice = activityNoticeRepository.findByIdForUpdate(noticeId)
                .orElseThrow(() -> new NotFoundException("공지를 찾을 수 없습니다."));
        activityAccessGuard.requireView(notice.getActivity(), VIEW_DENIED);
        saveReadReceipt(userId, notice);
    }

    private void saveReadReceipt(UUID userId, ActivityNotice notice) {
        if (activityNoticeReadRepository.existsByNoticeIdAndUserId(notice.getId(), userId)) return;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("학회원을 찾을 수 없습니다."));
        activityNoticeReadRepository.save(ActivityNoticeRead.builder()
                .notice(notice)
                .user(user)
                .readAt(LocalDateTime.now())
                .build());
    }

    @Transactional
    public void delete(UUID id) {
        ActivityNotice notice = findOrThrow(id);
        activityAccessGuard.requireManage(notice.getActivity(), MANAGE_DENIED);
        activityNoticeReadRepository.deleteByNoticeId(id);
        activityNoticeRepository.delete(notice);
    }

    private ActivityNotice findOrThrow(UUID id) {
        return activityNoticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("공지를 찾을 수 없습니다."));
    }

    private Activity findActivityOrThrow(UUID activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("활동을 찾을 수 없습니다."));
    }

    private String normalizeContent(String content) {
        if (content == null || content.isBlank()) return null;
        return content.trim();
    }

    private ActivityNoticeResponseDto toResponseDto(ActivityNotice notice, boolean read) {
        return ActivityNoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .activityId(notice.getActivity().getId())
                .read(read)
                .createdAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .build();
    }
}
