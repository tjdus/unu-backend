package sogang.cnu.backend.quarter;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.common.domain.BaseEntity;
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

    private int year;

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

    public void update(QuarterRequestDto requestDto) {
        this.year = requestDto.getYear();
        this.season = Season.valueOf(requestDto.getSeason());
        this.startDate = requestDto.getStartDate();
        this.endDate = requestDto.getEndDate();
        updateName();
    }

}