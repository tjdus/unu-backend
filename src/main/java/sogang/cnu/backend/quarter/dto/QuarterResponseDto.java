package sogang.cnu.backend.quarter.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class QuarterResponseDto {
    private Long id;
    private String name;
    private int year;
    private String season;

    private LocalDate startDate;
    private LocalDate endDate;
}
