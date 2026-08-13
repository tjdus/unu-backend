package sogang.cnu.backend.activity;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity extends BaseEntity {
    private static final int DEFAULT_DEPOSIT_AMOUNT = 30_000;
    private static final int DEFAULT_LECTURE_PARTICIPANT_LIMIT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private ActivityStatus status;

    @ManyToOne
    @JoinColumn(name = "activity_type_id")
    private ActivityType activityType;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @ManyToOne
    @JoinColumn(name = "quarter_id")
    private Quarter quarter;

    private LocalDate startDate;
    private LocalDate endDate;

    private LocalDate recruitmentStartDate;
    private LocalDate recruitmentEndDate;

    @Column(name = "is_listed")
    @Builder.Default
    private Boolean listed = true;

    @Column(name = "deposit_amount")
    private Integer depositAmount;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    /** 추가 팀원을 모집할 때 희망하는 포지션 안내 */
    @Column(name = "recruitment_positions", columnDefinition = "TEXT")
    private String recruitmentPositions;

    /** 강의자 경력. 강의 개설 신청에서 받아 승인 시 그대로 가져온다. */
    @Column(name = "instructor_career", columnDefinition = "TEXT")
    private String instructorCareer;

    /** 강의계획서·스터디계획서. 개설 신청의 운영 계획서를 승인 시 그대로 가져온다. */
    @Column(name = "operation_plan", columnDefinition = "TEXT")
    private String operationPlan;

    /** 활동 내용에서 안내할 디스코드 초대 링크 (선택) */
    @Column(name = "discord_url", length = 2048)
    private String discordUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_activity_id")
    private Activity parentActivity;

    public void update(ActivityUpdateCommand command) {
        this.title = command.getTitle();
        this.description = command.getDescription();
        this.status = command.getStatus();
        this.startDate = command.getStartDate();
        this.endDate = command.getEndDate();
        this.activityType = command.getActivityType();
        this.assignee = command.getAssignee();
        this.quarter = command.getQuarter();
        this.parentActivity = command.getParentActivity();
        this.depositAmount = command.getDepositAmount();
        this.participantLimit = command.getParticipantLimit();
        this.recruitmentPositions = command.getRecruitmentPositions();
        this.discordUrl = command.getDiscordUrl();
        this.operationPlan = command.getOperationPlan();
        this.instructorCareer = command.getInstructorCareer();
        this.recruitmentStartDate = command.getRecruitmentStartDate();
        this.recruitmentEndDate = command.getRecruitmentEndDate();
        if (command.getListed() != null) {
            this.listed = command.getListed();
        }
    }

    public void updateStatus(ActivityStatus newStatus) {
        this.status = newStatus;
    }

    public void restoreOpeningDetails(String operationPlan, String instructorCareer) {
        if ((this.operationPlan == null || this.operationPlan.isBlank())
                && operationPlan != null && !operationPlan.isBlank()) {
            this.operationPlan = operationPlan.trim();
        }
        if ((this.instructorCareer == null || this.instructorCareer.isBlank())
                && instructorCareer != null && !instructorCareer.isBlank()) {
            this.instructorCareer = instructorCareer.trim();
        }
    }

    /** 스터디는 담당자도 함께 공부하므로 참여자로 등록하고 정원에도 포함한다. */
    public boolean includesAssigneeAsParticipant() {
        return activityType != null && "STUDY".equals(activityType.getCode());
    }

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivityParticipant> participants = new java.util.ArrayList<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ActivitySession> sessions = new java.util.ArrayList<>();

    public static Activity create(ActivityCreateCommand command) {
        Activity activity = Activity.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .status(command.getStatus())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .recruitmentStartDate(command.getRecruitmentStartDate())
                .recruitmentEndDate(command.getRecruitmentEndDate())
                .listed(command.getListed() == null ? true : command.getListed())
                .activityType(command.getActivityType())
                .assignee(command.getAssignee())
                .quarter(command.getQuarter())
                .parentActivity(command.getParentActivity())
                .depositAmount(defaultDepositAmount(
                        command.getActivityType(),
                        command.getDepositAmount()
                ))
                .participantLimit(defaultParticipantLimit(
                        command.getActivityType(),
                        command.getParticipantLimit()
                ))
                .recruitmentPositions(command.getRecruitmentPositions())
                .discordUrl(command.getDiscordUrl())
                .operationPlan(command.getOperationPlan())
                .instructorCareer(command.getInstructorCareer())
                .build();
        return activity;
    }

    public Integer getDepositAmount() {
        return defaultDepositAmount(activityType, depositAmount);
    }

    public Integer getParticipantLimit() {
        return defaultParticipantLimit(activityType, participantLimit);
    }

    private static Integer defaultDepositAmount(ActivityType activityType, Integer amount) {
        if (amount != null) return amount;
        if (activityType == null) return 0;
        String code = activityType.getCode();
        return "STUDY".equals(code) || "SPECIAL_LECTURE".equals(code)
                ? DEFAULT_DEPOSIT_AMOUNT
                : 0;
    }

    private static Integer defaultParticipantLimit(ActivityType activityType, Integer limit) {
        if (limit != null) return limit;
        return activityType != null && "LECTURE".equals(activityType.getCode())
                ? DEFAULT_LECTURE_PARTICIPANT_LIMIT
                : null;
    }
}
