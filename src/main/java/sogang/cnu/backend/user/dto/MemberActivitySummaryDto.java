package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class MemberActivitySummaryDto {
    private UUID userId;
    private String name;
    private String studentId;
    private UUID joinedQuarterId;
    private String joinedQuarterName;
    private String evaluationEndQuarterName;
    private LocalDate joinedQuarterEndDate;
    private LocalDate evaluationEndDate;
    private int totalCompletedCount;
    private int joinedQuarterCompletedCount;
    private int evaluationCompletedQuarterCount;
    private String policyStatus;
    private String policyReason;
    private List<CompletedActivity> completedActivities;

    public record CompletedActivity(
            UUID activityId,
            String title,
            String quarterName,
            boolean joinedQuarter,
            boolean evaluationPeriod
    ) {}
}
