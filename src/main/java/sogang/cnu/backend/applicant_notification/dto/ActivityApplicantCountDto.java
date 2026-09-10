package sogang.cnu.backend.applicant_notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityApplicantCountDto {
    private UUID activityId;
    private String activityTitle;
    private long newApplicantCount;
}
