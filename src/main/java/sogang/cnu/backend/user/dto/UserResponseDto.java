package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private Long id;
    private String name;
    private String username;
    private String studentId;
    private String email;
    private String phoneNumber;
    private String githubId;
}
