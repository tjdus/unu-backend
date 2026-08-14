package sogang.cnu.backend.menu_notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuNotificationSummaryDto {
    private long activityCount;
    private long operationRecruitmentCount;
    private List<UUID> newActivityIds;
    private List<UUID> newOperationRecruitmentIds;
}
