package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.UUID;

/**
 * 활동에 딸린 자료·공지처럼 "활동 단위"로 권한이 갈리는 리소스의 공통 규칙.
 * 강의자료와 활동 공지가 같은 규칙을 쓰므로 한곳에 모아둔다.
 */
@Component
@RequiredArgsConstructor
public class ActivityAccessGuard {

    private final ActivityParticipantRepository activityParticipantRepository;

    /** 운영진이거나 그 활동의 담당자인지. */
    public boolean canManage(Activity activity) {
        if (SecurityUtils.isManagerOrAdmin()) return true;

        return activity != null &&
                activity.getAssignee() != null &&
                activity.getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
    }

    /**
     * 관리 권한 요구. 활동에 연결되지 않은 공용 리소스는 운영진만 다룰 수 있다.
     */
    public void requireManage(Activity activity, String message) {
        if (!canManage(activity)) {
            throw new ForbiddenException(message);
        }
    }

    /** 관리 권한이 있거나, 그 활동에 참여 확정(APPROVED)된 회원인지. */
    public boolean canView(Activity activity) {
        if (canManage(activity)) return true;
        if (activity == null) return false;

        UUID userId = SecurityUtils.getCurrentUserId();
        return activityParticipantRepository
                .findByUserIdAndActivityId(userId, activity.getId())
                .filter(participant -> participant.getStatus() == ActivityParticipantStatus.APPROVED)
                .isPresent();
    }

    public void requireView(Activity activity, String message) {
        if (!canView(activity)) {
            throw new ForbiddenException(message);
        }
    }
}
