package sogang.cnu.backend.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class NoticeResponseDto {
    private UUID id;
    private String title;
    private String tag;
    private String content;
    private String createdAt;
}
