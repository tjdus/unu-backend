package sogang.cnu.backend.user_role.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.role.dto.RoleResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class UserRoleResponseDto {
    private Long id;
    private UserResponseDto user;
    private RoleResponseDto role;
}
