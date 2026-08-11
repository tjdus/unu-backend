package sogang.cnu.backend.activity_participant;

import jakarta.persistence.*;
import lombok.*;

import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;

import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.user.User;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activity_participants",
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

    @Column(name = "promotion_agreed_at")
    private LocalDateTime promotionAgreedAt;

    public void updateStatus(ActivityParticipantStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ActivityParticipantStatus.APPROVED) {
            this.joinedAt = LocalDateTime.now();
        }
    }

    public void updateCompleted() {
        this.completed = true;
        this.completedAt = LocalDateTime.now();
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
        this.promotionAgreedAt = promotionAgreed ? now : null;
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
