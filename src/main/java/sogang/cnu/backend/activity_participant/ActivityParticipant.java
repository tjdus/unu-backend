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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public void update(ActivityParticipantUpdateCommand command) {
        this.status = command.getStatus();
        this.completed = command.getCompleted();
    }

    public static ActivityParticipant create(ActivityParticipantCreateCommand command) {
        return ActivityParticipant.builder()
                .activity(command.getActivity())
                .user(command.getUser())
                .status(command.getStatus())
                .build();
    }
}
