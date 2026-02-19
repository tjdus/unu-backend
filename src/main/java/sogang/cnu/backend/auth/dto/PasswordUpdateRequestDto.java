package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PasswordUpdateRequestDto {
    private String currentPassword;
    private String newPassword;
}
