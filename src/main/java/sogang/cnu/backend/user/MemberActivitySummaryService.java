package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.CurrentQuarter;
import sogang.cnu.backend.quarter.CurrentQuarterRepository;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.quarter.Season;
import sogang.cnu.backend.user.dto.MemberActivitySummaryDto;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberActivitySummaryService {
    private final UserRepository userRepository;
    private final QuarterRepository quarterRepository;
    private final ActivityParticipantRepository participantRepository;
    private final CurrentQuarterRepository currentQuarterRepository;

    @Transactional(readOnly = true)
    public List<MemberActivitySummaryDto> getAll() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Map<Integer, Quarter> quarters = quarterRepository.findAll().stream()
                .filter(quarter -> quarterIndex(quarter) != null)
                .collect(Collectors.toMap(MemberActivitySummaryService::quarterIndex, quarter -> quarter));
        Quarter currentQuarter = currentQuarterRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .map(CurrentQuarter::getQuarter).orElse(null);
        Map<UUID, List<ActivityParticipant>> participants = participantRepository.findForMemberSummary()
                .stream().collect(Collectors.groupingBy(participant -> participant.getUser().getId()));

        return userRepository.findAll().stream()
                .filter(user -> user.getMemberStatus() != MemberStatus.REMOVED)
                .filter(user -> user.getUserRoles().stream()
                        .noneMatch(userRole -> "ALUMNI".equals(userRole.getRole().getName())))
                .map(user -> summarize(user, participants.getOrDefault(user.getId(), List.of()), quarters, today, currentQuarter))
                .toList();
    }

    static MemberActivitySummaryDto summarize(
            User user,
            List<ActivityParticipant> participants,
            Map<Integer, Quarter> quarters,
            LocalDate today,
            Quarter currentQuarter
    ) {
        Quarter joined = user.getJoinedQuarter();
        Integer joinedIndex = quarterIndex(joined);
        Quarter lastQuarter = joinedIndex == null ? null : quarters.get(joinedIndex + 3);
        LocalDate joinedEnd = joined == null ? null : joined.getEndDate();
        LocalDate evaluationEnd = lastQuarter == null ? null : lastQuarter.getEndDate();
        HashSet<UUID> countedActivityIds = new HashSet<>();
        HashSet<Integer> evaluationQuarterIndexes = new HashSet<>();
        List<MemberActivitySummaryDto.CompletedActivity> activities = participants.stream()
                .filter(participant -> Boolean.TRUE.equals(participant.getCompleted())
                        && participant.getStatus() == ActivityParticipantStatus.APPROVED)
                .filter(participant -> countedActivityIds.add(participant.getActivity().getId()))
                .sorted(Comparator.comparing(
                        (ActivityParticipant participant) -> quarterIndex(participant.getActivity().getQuarter()),
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(participant -> {
                    var activity = participant.getActivity();
                    Integer index = quarterIndex(activity.getQuarter());
                    boolean evaluationPeriod = joinedIndex != null && index != null
                            && index >= joinedIndex && index < joinedIndex + 4;
                    if (evaluationPeriod) evaluationQuarterIndexes.add(index);
                    return new MemberActivitySummaryDto.CompletedActivity(
                            activity.getId(), activity.getTitle(),
                            activity.getQuarter() == null ? null : activity.getQuarter().getName(),
                            joinedIndex != null && joinedIndex.equals(index),
                            evaluationPeriod);
                }).toList();

        int joinedCount = (int) activities.stream().filter(MemberActivitySummaryDto.CompletedActivity::joinedQuarter).count();
        int evaluationQuarterCount = evaluationQuarterIndexes.size();
        Integer currentIndex = quarterIndex(currentQuarter);
        boolean finalQuarterWithoutParticipation = joinedIndex != null && currentIndex != null
                && currentIndex == joinedIndex + 3
                && participants.stream().noneMatch(participant ->
                    (participant.getStatus() == ActivityParticipantStatus.APPROVED
                            || participant.getStatus() == ActivityParticipantStatus.APPLIED)
                    && currentIndex.equals(quarterIndex(participant.getActivity().getQuarter())));
        String status;
        String reason;
        if (joinedIndex == null) {
            status = "CHECK_REQUIRED";
            reason = "가입 분기를 확인해주세요.";
        } else if (joinedCount >= 1 && evaluationQuarterCount >= 2) {
            status = "FULFILLED";
            reason = "가입 분기에 수료한 활동이 있으며, 가입 분기를 포함한 4개 분기 중 2개 이상 분기에서 활동을 수료했습니다.";
        } else if ((joinedCount == 0 && joinedEnd != null && today.isAfter(joinedEnd))
                || (evaluationQuarterCount < 2 && evaluationEnd != null && today.isAfter(evaluationEnd))) {
            status = "REMOVAL_TARGET";
            reason = joinedCount == 0 && joinedEnd != null && today.isAfter(joinedEnd)
                    ? "가입 분기가 종료되었으나 해당 분기의 수료 활동이 없습니다."
                    : "가입 분기를 포함한 4개 분기가 종료되었으나 활동을 수료한 분기가 2개 미만입니다.";
        } else if (evaluationQuarterCount < 2 && finalQuarterWithoutParticipation) {
            status = "REMOVAL_TARGET";
            reason = "활동 인정 마지막 분기이며 활동을 수료한 분기가 2개 미만이고, 이번 분기에 참여하거나 신청한 활동이 없습니다.";
        } else if ((joinedCount == 0 && joinedEnd == null) || (evaluationQuarterCount < 2 && evaluationEnd == null)) {
            status = "CHECK_REQUIRED";
            reason = "가입 분기 또는 평가 마지막 분기의 종료일을 확인해주세요.";
        } else {
            status = "WITHIN_PERIOD";
            reason = "평가 기간 내이며 아직 수료 조건을 충족하지 않았습니다.";
        }

        return MemberActivitySummaryDto.builder()
                .userId(user.getId()).name(user.getName()).studentId(user.getStudentId())
                .joinedQuarterId(joined == null ? null : joined.getId())
                .joinedQuarterName(joined == null ? null : joined.getName())
                .evaluationEndQuarterName(joinedIndex == null ? null : quarterName(joinedIndex + 3))
                .joinedQuarterEndDate(joinedEnd).evaluationEndDate(evaluationEnd)
                .totalCompletedCount(activities.size()).joinedQuarterCompletedCount(joinedCount)
                .evaluationCompletedQuarterCount(evaluationQuarterCount).policyStatus(status).policyReason(reason)
                .completedActivities(activities).build();
    }

    private static Integer quarterIndex(Quarter quarter) {
        if (quarter == null || quarter.getYear() == null || quarter.getSeason() == null
                || quarter.getSeason() == Season.PRECEDENCE) return null;
        return quarter.getYear() * 4 + quarter.getSeason().getOrder();
    }

    private static String quarterName(int index) {
        return index / 4 + " " + Season.values()[index % 4].name();
    }
}
