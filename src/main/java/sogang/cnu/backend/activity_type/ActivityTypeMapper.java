package sogang.cnu.backend.activity_type;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import sogang.cnu.backend.activity_type.dto.ActivityTypeRequestDto;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;

@Mapper(componentModel = "spring")
public interface ActivityTypeMapper {

    ActivityTypeMapper INSTANCE = Mappers.getMapper(ActivityTypeMapper.class);

    ActivityTypeResponseDto toResponseDto(ActivityType activityType);

    ActivityType toEntity(ActivityTypeRequestDto activityTypeRequestDto);
}
