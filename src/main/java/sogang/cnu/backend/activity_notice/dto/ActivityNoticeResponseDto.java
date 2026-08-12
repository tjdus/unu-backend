package sogang.cnu.backend.activity_notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ActivityNoticeResponseDto {
    private UUID id;
    private String title;
    private String content;
    private UUID activityId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
