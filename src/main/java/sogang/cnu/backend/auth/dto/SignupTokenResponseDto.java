package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SignupTokenResponseDto {
    private String token;
    private LocalDateTime expiresAt;
}