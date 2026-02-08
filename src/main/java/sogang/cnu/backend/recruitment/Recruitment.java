package sogang.cnu.backend.recruitment;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.recruitment.command.RecruitmentCreateCommand;
import sogang.cnu.backend.recruitment.command.RecruitmentUpdateCommand;

import java.time.LocalDateTime;

@Entity
@Table(name = "recruitments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recruitment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id")
    private Quarter quarter;

    @Column(nullable = false)
    private Boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    public void update(RecruitmentUpdateCommand command) {
        this.title = command.getTitle();
        this.description = command.getDescription();
        this.startAt = command.getStartAt();
        this.endAt = command.getEndAt();
        this.quarter = command.getQuarter();
        this.active = command.getActive();
        this.form = command.getForm();
    }

    public static Recruitment create(RecruitmentCreateCommand command) {
        return Recruitment.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .startAt(command.getStartAt())
                .endAt(command.getEndAt())
                .quarter(command.getQuarter())
                .active(command.getActive())
                .form(command.getForm())
                .build();
    }
}

