package sogang.cnu.backend.activity_notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class ActivityNoticeRequestDto {
    @NotNull(message = "활동을 지정해주세요.")
    private UUID activityId;

    @NotBlank(message = "공지 제목을 입력해주세요.")
    @Size(max = 120, message = "공지 제목은 120자 이내로 입력해주세요.")
    private String title;

    @Size(max = 5000, message = "공지 내용은 5000자 이내로 입력해주세요.")
    private String content;
}
