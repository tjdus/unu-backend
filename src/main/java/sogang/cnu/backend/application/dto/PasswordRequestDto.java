package sogang.cnu.backend.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PasswordRequestDto {
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
