package sogang.cnu.backend.auth.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private String nickname;
}
