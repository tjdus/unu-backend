package sogang.cnu.backend.auth.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDTO {
    private String name;
    private String loginId;
    private String password;
    private String studentId;
    private String githubId;
    private String phoneNumber;
    private String email;
    private Long joinedQuarterId;
}
