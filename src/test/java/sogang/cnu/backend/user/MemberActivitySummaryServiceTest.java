package sogang.cnu.backend.user;

import org.junit.jupiter.api.Test;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.quarter.CurrentQuarterRepository;
import sogang.cnu.backend.quarter.Season;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.user_role.UserRole;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MemberActivitySummaryServiceTest {
    private final Quarter fall = quarter(2026, Season.FALL, LocalDate.of(2026, 12, 31));
    private final Quarter summer = quarter(2027, Season.SUMMER, LocalDate.of(2027, 8, 31));
    private final Map<Integer, Quarter> quarters = Map.of(2026 * 4 + 3, fall, 2027 * 4 + 2, summer);

    @Test
    void alumniRoleIsExcludedEvenWhenMemberRoleIsAlsoAssigned() {
        var userRepository = mock(UserRepository.class);
        var quarterRepository = mock(QuarterRepository.class);
        var participantRepository = mock(ActivityParticipantRepository.class);
        var currentQuarterRepository = mock(CurrentQuarterRepository.class);
        var alumni = user(fall);
        alumni.getUserRoles().add(UserRole.builder().role(Role.builder().name("MEMBER").build()).build());
        alumni.getUserRoles().add(UserRole.builder().role(Role.builder().name("ALUMNI").build()).build());
        var member = user(fall);
        when(userRepository.findAll()).thenReturn(List.of(alumni, member));
        when(quarterRepository.findAll()).thenReturn(List.of(fall, summer));
        when(participantRepository.findForMemberSummary()).thenReturn(List.of());
        when(currentQuarterRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")))
                .thenReturn(Optional.empty());

        var service = new MemberActivitySummaryService(userRepository, quarterRepository,
                participantRepository, currentQuarterRepository);

        assertThat(service.getAll()).extracting(result -> result.getUserId()).containsExactly(member.getId());
    }

    @Test
    void countsFourQuartersAcrossYearsAndExcludesFollowingFall() {
        var result = MemberActivitySummaryService.summarize(user(fall), List.of(
                participant(fall), participant(summer),
                participant(quarter(2027, Season.FALL, LocalDate.of(2027, 12, 31)))
        ), quarters, LocalDate.of(2027, 10, 1), null);

        assertThat(result.getTotalCompletedCount()).isEqualTo(3);
        assertThat(result.getJoinedQuarterCompletedCount()).isEqualTo(1);
        assertThat(result.getEvaluationCompletedQuarterCount()).isEqualTo(2);
        assertThat(result.getEvaluationEndQuarterName()).isEqualTo("2027 SUMMER");
        assertThat(result.getPolicyStatus()).isEqualTo("FULFILLED");
    }

    @Test
    void multipleCompletionsInJoiningQuarterCountAsOneQuarterAndDeduplicateActivity() {
        ActivityParticipant first = participant(fall);
        var result = MemberActivitySummaryService.summarize(user(fall), List.of(first, first, participant(fall)),
                quarters, LocalDate.of(2026, 10, 9), null);

        assertThat(result.getTotalCompletedCount()).isEqualTo(2);
        assertThat(result.getJoinedQuarterCompletedCount()).isEqualTo(2);
        assertThat(result.getEvaluationCompletedQuarterCount()).isEqualTo(1);
        assertThat(result.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
    }

    @Test
    void failingJoiningQuarterCannotBeRepairedByLaterCompletions() {
        var result = MemberActivitySummaryService.summarize(user(fall), List.of(participant(summer), participant(summer)),
                quarters, LocalDate.of(2027, 8, 1), null);

        assertThat(result.getEvaluationCompletedQuarterCount()).isEqualTo(1);
        assertThat(result.getPolicyStatus()).isEqualTo("REMOVAL_TARGET");
    }

    @Test
    void deadlineIsInclusiveAndOnlyFollowingDayIsOverdue() {
        List<ActivityParticipant> records = List.of(participant(fall));
        var onDeadline = MemberActivitySummaryService.summarize(user(fall), records, quarters, summer.getEndDate(), null);
        var afterDeadline = MemberActivitySummaryService.summarize(user(fall), records, quarters, summer.getEndDate().plusDays(1), null);

        assertThat(onDeadline.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
        assertThat(afterDeadline.getPolicyStatus()).isEqualTo("REMOVAL_TARGET");
    }

    @Test
    void unknownJoiningQuarterOrMissingDeadlineRequiresReview() {
        var unknownJoining = MemberActivitySummaryService.summarize(user(null), List.of(), quarters, LocalDate.of(2026, 10, 9), null);
        var missingDeadline = MemberActivitySummaryService.summarize(user(fall), List.of(participant(fall)), Map.of(), LocalDate.of(2026, 10, 9), null);

        assertThat(unknownJoining.getPolicyStatus()).isEqualTo("CHECK_REQUIRED");
        assertThat(missingDeadline.getPolicyStatus()).isEqualTo("CHECK_REQUIRED");
    }

    @Test
    void activitiesBeforeJoiningDoNotCountTowardsRequirements() {
        var prior = quarter(2026, Season.SUMMER, LocalDate.of(2026, 8, 31));
        var result = MemberActivitySummaryService.summarize(user(fall), List.of(participant(prior)), quarters, LocalDate.of(2026, 10, 9), null);

        assertThat(result.getTotalCompletedCount()).isEqualTo(1);
        assertThat(result.getEvaluationCompletedQuarterCount()).isZero();
        assertThat(result.getJoinedQuarterCompletedCount()).isZero();
        assertThat(result.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
    }

    @Test
    void incompleteAndRejectedParticipationDoesNotCount() {
        ActivityParticipant incomplete = participant(fall);
        incomplete.setCompleted(false);
        ActivityParticipant rejected = participant(fall);
        rejected.setStatus(ActivityParticipantStatus.REJECTED);
        var result = MemberActivitySummaryService.summarize(user(fall), List.of(incomplete, rejected),
                quarters, LocalDate.of(2026, 10, 9), null);

        assertThat(result.getTotalCompletedCount()).isZero();
        assertThat(result.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
    }

    @Test
    void finalQuarterWithoutParticipationIsRemovalTargetBeforeDeadline() {
        Quarter winter = quarter(2026, Season.WINTER, LocalDate.of(2026, 2, 28));
        var result = MemberActivitySummaryService.summarize(user(winter), List.of(participant(winter), participant(winter)),
                Map.of(2026 * 4, winter, 2026 * 4 + 3, fall), LocalDate.of(2026, 10, 9), fall);

        assertThat(result.getTotalCompletedCount()).isEqualTo(2);
        assertThat(result.getEvaluationCompletedQuarterCount()).isEqualTo(1);
        assertThat(result.getPolicyStatus()).isEqualTo("REMOVAL_TARGET");
    }

    @Test
    void finalQuarterParticipationOrPendingApplicationKeepsOpportunityToComplete() {
        Quarter winter = quarter(2026, Season.WINTER, LocalDate.of(2026, 2, 28));
        for (ActivityParticipantStatus status : List.of(ActivityParticipantStatus.APPROVED, ActivityParticipantStatus.APPLIED)) {
            var current = participant(fall);
            current.setCompleted(false);
            current.setStatus(status);
            var result = MemberActivitySummaryService.summarize(user(winter), List.of(participant(winter), current),
                    Map.of(2026 * 4, winter, 2026 * 4 + 3, fall), LocalDate.of(2026, 10, 9), fall);

            assertThat(result.getEvaluationCompletedQuarterCount()).isEqualTo(1);
            assertThat(result.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
        }
    }

    @Test
    void rejectedApplicationDoesNotPreventRemovalAndEarlierQuarterStillHasTime() {
        Quarter winter = quarter(2026, Season.WINTER, LocalDate.of(2026, 2, 28));
        var rejected = participant(fall);
        rejected.setStatus(ActivityParticipantStatus.REJECTED);
        var records = List.of(participant(winter), rejected);
        var quarterMap = Map.of(2026 * 4, winter, 2026 * 4 + 3, fall);
        var inFinalQuarter = MemberActivitySummaryService.summarize(user(winter), records, quarterMap,
                LocalDate.of(2026, 10, 9), fall);
        var beforeFinalQuarter = MemberActivitySummaryService.summarize(user(winter), records, quarterMap,
                LocalDate.of(2026, 7, 1), quarter(2026, Season.SUMMER, LocalDate.of(2026, 8, 31)));

        assertThat(inFinalQuarter.getPolicyStatus()).isEqualTo("REMOVAL_TARGET");
        assertThat(beforeFinalQuarter.getPolicyStatus()).isEqualTo("WITHIN_PERIOD");
    }

    private static Quarter quarter(int year, Season season, LocalDate end) {
        return Quarter.builder().id(UUID.randomUUID()).year(year).season(season)
                .name(year + " " + season.name()).endDate(end).build();
    }

    private static User user(Quarter joined) {
        return User.builder().id(UUID.randomUUID()).name("Member").studentId("20261234").joinedQuarter(joined).build();
    }

    private static ActivityParticipant participant(Quarter quarter) {
        return ActivityParticipant.builder().completed(true).status(ActivityParticipantStatus.APPROVED)
                .activity(Activity.builder().id(UUID.randomUUID()).title("Activity").quarter(quarter).build()).build();
    }
}
