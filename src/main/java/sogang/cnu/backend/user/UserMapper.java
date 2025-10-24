package sogang.cnu.backend.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sogang.cnu.backend.user.dto.UserRequestDto;
import sogang.cnu.backend.user.dto.UserResponseDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponseDto toUserResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    User toUserEntity(UserRequestDto userRequestDto);
}
