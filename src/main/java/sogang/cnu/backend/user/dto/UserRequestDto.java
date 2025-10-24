package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRequestDto {
    private String name;
    private String username;
    private String password;
    private String studentId;
    private String githubId;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
    private Long joinedQuarterId;
}
