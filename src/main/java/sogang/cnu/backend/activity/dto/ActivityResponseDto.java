package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivityResponseDto {
    private Long id;
    private String title;
    private String description;
    private String status;
    private ActivityTypeResponseDto activityType;
    private UserResponseDto assignee;
    private QuarterResponseDto quarter;
    private String startDate;
    private String endDate;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;

}
