package sogang.cnu.backend.activity_notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityAccessGuard;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeRequestDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityNoticeService {

    private static final String VIEW_DENIED = "해당 활동의 공지를 볼 권한이 없습니다.";
    private static final String MANAGE_DENIED = "해당 활동의 공지를 관리할 권한이 없습니다.";

    private final ActivityNoticeRepository activityNoticeRepository;
    private final ActivityRepository activityRepository;
    private final ActivityAccessGuard activityAccessGuard;

    @Transactional(readOnly = true)
    public List<ActivityNoticeResponseDto> getByActivityId(UUID activityId) {
        Activity activity = findActivityOrThrow(activityId);
        activityAccessGuard.requireView(activity, VIEW_DENIED);

        return activityNoticeRepository.findAllByActivityIdOrderByCreatedAtDesc(activityId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional
    public ActivityNoticeResponseDto create(ActivityNoticeRequestDto request) {
        Activity activity = findActivityOrThrow(request.getActivityId());
        activityAccessGuard.requireManage(activity, MANAGE_DENIED);

        ActivityNotice notice = ActivityNotice.builder()
                .title(request.getTitle().trim())
                .content(normalizeContent(request.getContent()))
                .activity(activity)
                .build();
        return toResponseDto(activityNoticeRepository.save(notice));
    }

    @Transactional
    public ActivityNoticeResponseDto update(UUID id, ActivityNoticeRequestDto request) {
        ActivityNotice notice = findOrThrow(id);
        activityAccessGuard.requireManage(notice.getActivity(), MANAGE_DENIED);

        notice.update(request.getTitle().trim(), normalizeContent(request.getContent()));
        return toResponseDto(notice);
    }

    @Transactional
    public void delete(UUID id) {
        ActivityNotice notice = findOrThrow(id);
        activityAccessGuard.requireManage(notice.getActivity(), MANAGE_DENIED);
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

    private ActivityNoticeResponseDto toResponseDto(ActivityNotice notice) {
        return ActivityNoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .activityId(notice.getActivity().getId())
                .createdAt(notice.getCreatedAt())
                .modifiedAt(notice.getModifiedAt())
                .build();
    }
}
