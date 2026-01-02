package sogang.cnu.backend.activity;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;

@Entity
@Table(name = "activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Activity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public void update(ActivityUpdateCommand command) {
        this.title = command.getTitle();
        this.description = command.getDescription();
        this.status = command.getStatus();
        this.startDate = command.getStartDate();
        this.endDate = command.getEndDate();
        this.activityType = command.getActivityType();
        this.assignee = command.getAssignee();
        this.quarter = command.getQuarter();
    }

    public static Activity create(ActivityCreateCommand command) {
        Activity activity = Activity.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .status(command.getStatus())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .activityType(command.getActivityType())
                .assignee(command.getAssignee())
                .quarter(command.getQuarter())
                .build();
        return activity;
    }
}
