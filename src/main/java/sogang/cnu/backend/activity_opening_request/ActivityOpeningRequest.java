package sogang.cnu.backend.activity_opening_request;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "activity_opening_requests")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityOpeningRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "operation_plan", columnDefinition = "TEXT", nullable = false)
    private String operationPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_type_id", nullable = false)
    private ActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "expected_member_count", nullable = false)
    private Integer expectedMemberCount;

    @Column(name = "accepts_new_members", nullable = false)
    private Boolean acceptsNewMembers;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "recruitment_positions", columnDefinition = "TEXT")
    private String recruitmentPositions;

    /** 강의 개설 신청 시 받는 강의자 경력 */
    @Column(name = "instructor_career", columnDefinition = "TEXT")
    private String instructorCareer;

    @Column(name = "is_personal_project")
    @Builder.Default
    private Boolean personalProject = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_activity_id")
    private Activity parentActivity;

    @ManyToMany
    @JoinTable(
            name = "activity_opening_request_members",
            joinColumns = @JoinColumn(name = "request_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> initialMembers = new LinkedHashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityOpeningRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_activity_id", unique = true)
    private Activity approvedActivity;

    public void update(
            String title,
            String description,
            String operationPlan,
            ActivityType activityType,
            Quarter quarter,
            LocalDate startDate,
            LocalDate endDate,
            Integer expectedMemberCount,
            Boolean acceptsNewMembers,
            Integer participantLimit,
            String recruitmentPositions,
            String instructorCareer,
            Boolean personalProject,
            Activity parentActivity,
            Set<User> initialMembers
    ) {
        this.title = title;
        this.description = description;
        this.operationPlan = operationPlan;
        this.activityType = activityType;
        this.quarter = quarter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expectedMemberCount = expectedMemberCount;
        this.acceptsNewMembers = acceptsNewMembers;
        this.participantLimit = participantLimit;
        this.recruitmentPositions = recruitmentPositions;
        this.instructorCareer = instructorCareer;
        this.personalProject = personalProject;
        this.parentActivity = parentActivity;
        this.initialMembers.clear();
        this.initialMembers.addAll(initialMembers);
    }

    public void submit() {
        this.status = ActivityOpeningRequestStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
        this.reviewer = null;
        this.reviewComment = null;
        this.reviewedAt = null;
    }

    public void cancel() {
        this.status = ActivityOpeningRequestStatus.CANCELED;
    }

    public void review(ActivityOpeningRequestStatus status, User reviewer, String comment) {
        this.status = status;
        this.reviewer = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
    }

    public void approve(User reviewer, String comment, Activity activity) {
        this.status = ActivityOpeningRequestStatus.APPROVED;
        this.reviewer = reviewer;
        this.reviewComment = comment;
        this.reviewedAt = LocalDateTime.now();
        this.approvedActivity = activity;
    }

    public Activity unlinkApprovedActivity() {
        Activity activity = this.approvedActivity;
        this.approvedActivity = null;
        return activity;
    }
}
