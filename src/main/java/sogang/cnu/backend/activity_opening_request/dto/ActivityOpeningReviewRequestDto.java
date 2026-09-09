package sogang.cnu.backend.activity_opening_request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityOpeningReviewRequestDto {
    @NotBlank(message = "검토 상태를 선택해주세요.")
    private String status;

    private String comment;
}
