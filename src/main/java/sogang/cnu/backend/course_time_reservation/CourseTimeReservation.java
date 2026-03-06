package sogang.cnu.backend.course_time_reservation;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.course_time_reservation.command.CourseTimeReservationCreateCommand;
import sogang.cnu.backend.course_time_reservation.command.CourseTimeReservationUpdateCommand;
import sogang.cnu.backend.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "course_time_reservations",
        indexes = {
                @Index(name = "idx_ctr_user_start", columnList = "user_id, start_at"),
                @Index(name = "idx_ctr_activity", columnList = "activity_id"),
                @Index(name = "idx_ctr_user_activity", columnList = "user_id, activity_id")
        }
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseTimeReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    public void update(CourseTimeReservationUpdateCommand command) {
        this.startAt = command.getStartAt();
        this.endAt = command.getEndAt();
    }

    public static CourseTimeReservation create(CourseTimeReservationCreateCommand command) {
        return CourseTimeReservation.builder()
                .activity(command.getActivity())
                .user(command.getUser())
                .startAt(command.getStartAt())
                .endAt(command.getEndAt())
                .build();
    }
}
