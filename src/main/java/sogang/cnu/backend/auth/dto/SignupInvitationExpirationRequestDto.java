package sogang.cnu.backend.auth.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SignupInvitationExpirationRequestDto {
    @NotNull(message = "만료 시간을 입력해주세요.")
    @Future(message = "만료 시간은 현재보다 이후여야 합니다.")
    private Instant expiresAt;
}
