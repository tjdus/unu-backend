package sogang.cnu.backend.quarter.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class QuarterResponseDto {
    private UUID id;
    private String name;
    private int year;
    private String season;

    private LocalDate startDate;
    private LocalDate endDate;
}
