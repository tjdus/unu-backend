package sogang.cnu.backend.activity_session.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivitySessionResponseDto {
    private Long id;
    private Long activityId;
    private Integer sessionNumber;
    private String date;
    private String description;
}
