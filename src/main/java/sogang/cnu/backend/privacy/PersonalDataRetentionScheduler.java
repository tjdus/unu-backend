package sogang.cnu.backend.privacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.application.Application;
import sogang.cnu.backend.application.ApplicationRepository;
import sogang.cnu.backend.application.ApplicationStatus;
import sogang.cnu.backend.auth.SignupInvitationMember;
import sogang.cnu.backend.auth.SignupInvitationMemberRepository;
import sogang.cnu.backend.recruitment.RecruitmentType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 개인정보 처리방침 제3조의 보유기간이 지난 정보를 매일 파기한다.
 * - 신입 지원서: 불합격자는 결정일로부터 1개월 이내, 그 외는 제출일로부터 6개월
 * - 지원서에서 복사한 합격자 가입 초대 정보: 6개월 (가입 완료 시에는 즉시 삭제)
 * - 활동 보증금 환급 계좌: 수집일로부터 6개월
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonalDataRetentionScheduler {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    private final ApplicationRepository applicationRepository;
    private final SignupInvitationMemberRepository invitationMemberRepository;
    private final ActivityParticipantRepository activityParticipantRepository;

    @Scheduled(cron = "0 30 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void purgeExpiredPersonalData() {
        LocalDateTime now = LocalDateTime.now(SEOUL);
        // 하루 한 번 실행되므로 하루 일찍 잡아 "1개월 이내"를 지킨다.
        LocalDateTime rejectedCutoff = now.minusMonths(1).plusDays(1);
        LocalDateTime sixMonthsAgo = now.minusMonths(6);

        List<Application> applications = applicationRepository.findExpiredForRetention(
                RecruitmentType.NEW_MEMBER, ApplicationStatus.REJECTED, rejectedCutoff, sixMonthsAgo);
        applicationRepository.deleteAll(applications);

        Instant sixMonthsAgoInstant = sixMonthsAgo.atZone(SEOUL).toInstant();
        List<SignupInvitationMember> members =
                invitationMemberRepository.findCopiedProfilesCreatedBefore(sixMonthsAgoInstant);
        members.forEach(SignupInvitationMember::clearProfile);

        List<ActivityParticipant> participants =
                activityParticipantRepository.findRefundAccountsCollectedBefore(sixMonthsAgo);
        participants.forEach(ActivityParticipant::clearRefundAccount);

        if (!applications.isEmpty() || !members.isEmpty() || !participants.isEmpty()) {
            log.info("Personal data retention: deleted {} applications, cleared {} invitation profiles, {} refund accounts",
                    applications.size(), members.size(), participants.size());
        }
    }
}
