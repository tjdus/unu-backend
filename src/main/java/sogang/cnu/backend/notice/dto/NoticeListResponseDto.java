package sogang.cnu.backend.notice.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NoticeListResponseDto {
    private List<NoticeResponseDto> notices;
    private long total;
}
