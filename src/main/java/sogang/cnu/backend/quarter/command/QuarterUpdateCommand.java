package sogang.cnu.backend.quarter.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.quarter.Season;

import java.time.LocalDate;

@Getter
@Builder
public class QuarterUpdateCommand {
    private Integer year;
    private Season season;
    private LocalDate startDate;
    private LocalDate endDate;
    private String name;
}

