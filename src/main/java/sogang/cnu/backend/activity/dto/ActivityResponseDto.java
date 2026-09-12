package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.util.UUID;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivityResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private ActivityTypeResponseDto activityType;
    private UserResponseDto assignee;
    private QuarterResponseDto quarter;
    private String startDate;
    private String endDate;
    private String recruitmentStartDate;
    private String recruitmentEndDate;
    private UUID parentActivityId;
    private Boolean listed;
    private Integer depositAmount;
    private Integer participantLimit;
    private String recruitmentPositions;
    private String discordUrl;
    private String operationPlan;
    private String instructorCareer;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;

}
