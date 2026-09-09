package sogang.cnu.backend.notice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NoticeRequestDto {
    private String title;
    private String tag;
    private String content;
}
