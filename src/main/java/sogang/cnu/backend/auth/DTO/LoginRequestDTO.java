package sogang.cnu.backend.auth.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    private String loginId;
    private String password;
}
