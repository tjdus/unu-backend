package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.user_role.dto.UserRoleResponseDto;

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
    private Boolean isActive;
    private QuarterResponseDto joinedQuarter;
    private UserRoleResponseDto[] userRoles;
}
