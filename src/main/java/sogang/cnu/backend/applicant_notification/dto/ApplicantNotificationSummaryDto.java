package sogang.cnu.backend.applicant_notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicantNotificationSummaryDto {
    private long totalCount;
    private LocalDateTime sinceCheckpoint;
    private List<ActivityApplicantCountDto> activities;
}
