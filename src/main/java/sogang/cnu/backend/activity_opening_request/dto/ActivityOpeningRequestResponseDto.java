package sogang.cnu.backend.activity_opening_request.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user.dto.UserSummaryResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ActivityOpeningRequestResponseDto {
    private UUID id;
    private UserSummaryResponseDto applicant;
    private String title;
    private String description;
    private String operationPlan;
    private String materialUrl;
    private ActivityTypeResponseDto activityType;
    private QuarterResponseDto quarter;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer expectedMemberCount;
    private Boolean acceptsNewMembers;
    private Integer participantLimit;
    private String recruitmentPositions;
    private String instructorCareer;
    private Boolean personalProject;
    private UUID parentActivityId;
    private String parentActivityTitle;
    private List<UserSummaryResponseDto> initialMembers;
    private String status;
    private UserSummaryResponseDto reviewer;
    private String reviewComment;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private UUID approvedActivityId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
