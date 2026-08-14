package sogang.cnu.backend.activity_notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ActivityNoticeUnreadCountDto {
    private UUID activityId;
    private long count;
}
