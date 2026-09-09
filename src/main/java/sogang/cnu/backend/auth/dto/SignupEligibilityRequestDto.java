package sogang.cnu.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupEligibilityRequestDto {
    @NotBlank(message = "학번을 입력해주세요.")
    @Pattern(regexp = "\\d{8}", message = "학번은 숫자 8자리로 입력해주세요.")
    private String studentId;
}
