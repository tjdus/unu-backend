package sogang.cnu.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {
    private String name;
    private String username;
    private String password;
    private String studentId;
    private String githubId;
    private String phoneNumber;
    private String email;
    private Long joinedQuarterId;
}
