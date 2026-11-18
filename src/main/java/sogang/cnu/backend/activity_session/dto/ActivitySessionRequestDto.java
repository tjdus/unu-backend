package sogang.cnu.backend.activity_session.dto;

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

    @NotNull(message = "날짜를 입력해주세요.")
    private LocalDate date;
    private String description;
}
