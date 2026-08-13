package sogang.cnu.backend.activity_opening_request.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActivityOpeningApprovalRequestDto {
    private String comment;
    @Min(value = 0, message = "보증금은 0원 이상이어야 합니다.")
    private Integer depositAmount;
}
