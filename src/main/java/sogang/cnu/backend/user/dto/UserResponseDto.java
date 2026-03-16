package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.UUID;
import sogang.cnu.backend.user_role.dto.UserRoleResponseDto;

@Getter
@Setter
@Builder
public class UserResponseDto {
    private UUID id;
    private String name;
    private String username;
    private String studentId;
    private String email;
    private String phoneNumber;
    private String major;
    private String subMajor;
    private String githubId;
    private Boolean isCurrentQuarterActive;
    private QuarterResponseDto joinedQuarter;
    private String memberStatus;
    private Boolean isAlumni;
    private UserRoleResponseDto[] userRoles;
}
