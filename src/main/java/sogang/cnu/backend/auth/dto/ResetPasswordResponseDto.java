package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResetPasswordResponseDto {
    private String temporaryPassword;
}
