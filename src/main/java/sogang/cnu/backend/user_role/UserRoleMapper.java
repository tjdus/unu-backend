package sogang.cnu.backend.user_role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.user_role.dto.UserRoleResponseDto;

@Mapper(componentModel = "spring")
public interface UserRoleMapper {

    @Mapping(source = "role", target = "role")
    UserRoleResponseDto toUserRoleResponseDto(UserRole userRole);
}
