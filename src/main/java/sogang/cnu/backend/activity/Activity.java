package sogang.cnu.backend.activity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity.dto.ActivityMappingDto;
import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Getter
@Setter
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

    public void update(ActivityMappingDto mappingDto) {
        this.title = mappingDto.getTitle();
        this.description = mappingDto.getDescription();
        this.status = ActivityStatus.valueOf(mappingDto.getStatus());
        this.startDate = mappingDto.getStartDate();
        this.endDate = mappingDto.getEndDate();
        this.activityType = mappingDto.getActivityType();
        this.assignee = mappingDto.getAssignee();
        this.quarter = mappingDto.getQuarter();
    }
}
