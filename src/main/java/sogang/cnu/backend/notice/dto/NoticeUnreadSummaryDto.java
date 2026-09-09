package sogang.cnu.backend.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class NoticeUnreadSummaryDto {
    private long totalCount;
    private List<UUID> noticeIds;
}
