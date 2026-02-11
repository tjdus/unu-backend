package sogang.cnu.backend.activity_session.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ActivitySessionRequestDto {
    private Long activityId;
    private Integer sessionNumber;
    private LocalDate date;
    private String description;
}
