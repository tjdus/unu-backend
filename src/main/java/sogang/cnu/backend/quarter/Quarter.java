package sogang.cnu.backend.quarter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.command.QuarterCreateCommand;
import sogang.cnu.backend.quarter.command.QuarterUpdateCommand;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;

import java.time.LocalDate;

@Entity
@Table(
        name = "quarters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"year", "season"}),
        indexes = @Index(name = "idx_year_season", columnList = "year, season")
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quarter extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer year;

    @Enumerated(EnumType.STRING)
    private Season season;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String name;

    @PrePersist
    @PreUpdate
    private void updateName() {
        if (year != 0 && season != null) {
            this.name = year + " " + season.name();
        }
    }

    public void update(QuarterUpdateCommand command) {
        this.year = command.getYear();
        this.season = command.getSeason();
        this.startDate = command.getStartDate();
        this.endDate = command.getEndDate();
        updateName();
    }

    public static Quarter create(QuarterCreateCommand command) {
        Quarter quarter = Quarter.builder()
                .year(command.getYear())
                .season(command.getSeason())
                .startDate(command.getStartDate())
                .endDate(command.getEndDate())
                .build();
        quarter.updateName();
        return quarter;
    }

}