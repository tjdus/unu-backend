package sogang.cnu.backend.user.DTO;

import lombok.Builder;

@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String loginId;
    private String studentId;
    private String email;
    private String phoneNumber;
    private String githubId;
}
