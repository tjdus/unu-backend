package sogang.cnu.backend.activity_session.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivitySessionRequestDto {
    @NotNull(message = "활동을 선택해주세요.")
    private UUID activityId;

    @NotNull(message = "회차를 입력해주세요.")
    @Min(value = 1, message = "회차는 1 이상이어야 합니다.")
    private Integer sessionNumber;

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;
    private String description;
}
