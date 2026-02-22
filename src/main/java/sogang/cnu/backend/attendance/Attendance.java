package sogang.cnu.backend.attendance;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.attendance.command.AttendanceCreateCommand;
import sogang.cnu.backend.attendance.command.AttendanceUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(
        name = "attendances",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_session_participant",
                        columnNames = {"session_id", "participant_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ActivitySession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private ActivityParticipant participant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    public void update(AttendanceUpdateCommand command) {
        this.status = command.getStatus();
    }

    public static Attendance create(AttendanceCreateCommand command) {
        return Attendance.builder()
                .session(command.getSession())
                .participant(command.getParticipant())
                .status(command.getStatus())
                .build();
    }
}
