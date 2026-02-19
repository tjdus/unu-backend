package sogang.cnu.backend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoRequestDto {
    private String name;
    private String username;
    private String studentId;
    private String githubId;
    private String phoneNumber;
    private String email;
}
