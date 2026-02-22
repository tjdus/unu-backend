package sogang.cnu.backend.user_role.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.role.dto.RoleResponseDto;

import java.util.UUID;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Getter
@Setter
@Builder
public class UserRoleResponseDto {
    private UUID id;
    private RoleResponseDto role;
}
