package sogang.cnu.backend.activity_participant;

import jakarta.persistence.*;
import lombok.*;

import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;

import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.user.User;


import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activity_participants",
        indexes = {
                @Index(
                        name = "idx_activity_participant_result_unread",
                        columnList = "user_id,result_read_at"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_activity_user",
                        columnNames = {"activity_id", "user_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityParticipant extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityParticipantStatus status;

    @Builder.Default
    @Column(nullable = false)
    private Boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "refund_bank_name", length = 50)
    private String refundBankName;

    @Column(name = "refund_account_number", length = 20)
    private String refundAccountNumber;

    @Column(name = "refund_account_holder", length = 50)
    private String refundAccountHolder;

    @Column(name = "deposit_policy_agreed_at")
    private LocalDateTime depositPolicyAgreedAt;

    @Column(name = "deposit_payment_confirmed_at")
    private LocalDateTime depositPaymentConfirmedAt;

    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    @Column(name = "promotion_agreed_at")
    private LocalDateTime promotionAgreedAt;

    @Column(name = "applied_position", length = 100)
    private String appliedPosition;

    @Column(name = "application_message", columnDefinition = "TEXT")
    private String applicationMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "lecture_participation_mode", length = 20)
    private LectureParticipationMode lectureParticipationMode;

    @Column(name = "review_message", length = 500)
    private String reviewMessage;

    @Column(name = "result_changed_at")
    private LocalDateTime resultChangedAt;

    @Column(name = "result_read_at")
    private LocalDateTime resultReadAt;

    // 본인이 참여를 신청(반려 후 재신청 포함)한 시각. 담당자 자동 등록·운영진 직접 추가는 신청이 아니라 비워 둔다.
    // 운영진 신청자 알림은 이 값을 기준으로 새 신청을 센다.
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    public void updateStatus(ActivityParticipantStatus newStatus) {
        if (this.status == newStatus) return;

        this.status = newStatus;
        if (newStatus == ActivityParticipantStatus.APPROVED) {
            this.joinedAt = LocalDateTime.now();
        } else {
            this.joinedAt = null;
        }
        if (newStatus != ActivityParticipantStatus.REJECTED) {
            this.reviewMessage = null;
        }
        if (newStatus == ActivityParticipantStatus.APPLIED) {
            this.resultChangedAt = null;
            this.resultReadAt = null;
        } else {
            this.resultChangedAt = LocalDateTime.now();
            this.resultReadAt = null;
        }
    }

    public void confirmOnActivityStart() {
        if (status != ActivityParticipantStatus.APPLIED || activity.getStartDate() == null) {
            return;
        }
        updateStatus(ActivityParticipantStatus.APPROVED);
        this.joinedAt = activity.getStartDate().atStartOfDay();
    }

    public void markApplied() {
        this.appliedAt = LocalDateTime.now();
    }

    public void markResultRead() {
        if (resultChangedAt != null && resultReadAt == null) {
            resultReadAt = LocalDateTime.now();
        }
    }

    public void updateCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }

    public void recordDepositApplication(
            String bankName,
            String accountNumber,
            String accountHolder,
            boolean promotionAgreed
    ) {
        LocalDateTime now = LocalDateTime.now();
        this.refundBankName = bankName;
        this.refundAccountNumber = accountNumber;
        this.refundAccountHolder = accountHolder;
        this.depositPolicyAgreedAt = now;
        this.depositPaymentConfirmedAt = now;
        this.privacyAgreedAt = now;
        this.promotionAgreedAt = promotionAgreed ? now : null;
    }

    public void recordProjectApplication(String position, String message) {
        this.appliedPosition = position;
        this.applicationMessage = message;
    }

    public void recordLectureParticipationMode(LectureParticipationMode mode) {
        this.lectureParticipationMode = mode;
    }

    public void recordReviewMessage(String message) {
        this.reviewMessage = message;
    }

    public void update(ActivityParticipantUpdateCommand command) {
        this.status = command.getStatus();
        this.completed = command.getCompleted();
    }

    public static ActivityParticipant create(ActivityParticipantCreateCommand command) {
        return ActivityParticipant.builder()
                .activity(command.getActivity())
                .user(command.getUser())
                .status(command.getStatus())
                .joinedAt(command.getStatus() == ActivityParticipantStatus.APPROVED
                        ? LocalDateTime.now()
                        : null)
                .build();
    }
}
