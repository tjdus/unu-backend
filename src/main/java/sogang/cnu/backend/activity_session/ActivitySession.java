package sogang.cnu.backend.activity_session;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity_session.command.ActivitySessionCreateCommand;
import sogang.cnu.backend.activity_session.command.ActivitySessionUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(
        name = "activity_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_activity_session_no",
                        columnNames = {"activity_id", "session_number"}
                ),
                @UniqueConstraint(
                        name = "uk_activity_session_date",
                        columnNames = {"activity_id", "date"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySession extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private Integer sessionNumber;

    @Column(nullable = false)
    private LocalDate date;

    @Column(columnDefinition = "TEXT")
    private String description;

    public void update(ActivitySessionUpdateCommand command) {
        this.sessionNumber = command.getSessionNumber();
        this.date = command.getDate();
        this.description = command.getDescription();
    }

    public static ActivitySession create(ActivitySessionCreateCommand command) {
        return ActivitySession.builder()
                .activity(command.getActivity())
                .sessionNumber(command.getSessionNumber())
                .date(command.getDate())
                .description(command.getDescription())
                .build();
    }
}
