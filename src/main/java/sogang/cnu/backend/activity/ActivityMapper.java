package sogang.cnu.backend.activity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sogang.cnu.backend.activity.dto.ActivityMappingDto;
import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;

@Mapper(componentModel = "spring")
public interface ActivityMapper {

    ActivityMapper INSTANCE = Mappers.getMapper(ActivityMapper.class);

    ActivityResponseDto toResponseDto(Activity activity);

    Activity toEntity(ActivityMappingDto activityMappingDto);
}
