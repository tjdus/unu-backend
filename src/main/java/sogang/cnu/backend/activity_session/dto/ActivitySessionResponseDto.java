package sogang.cnu.backend.activity_session.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;

import java.util.UUID;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class ActivitySessionResponseDto {
    private UUID id;
    private ActivityResponseDto activity;
    private Integer sessionNumber;
    private String date;
    private String description;
}
