package sogang.cnu.backend.activity_notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ActivityNoticeUnreadSummaryDto {
    private long totalCount;
    private List<ActivityNoticeUnreadCountDto> activities;
}
