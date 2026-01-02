package sogang.cnu.backend.activity_type;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;

@Mapper(componentModel = "spring")
public interface ActivityTypeMapper {
    ActivityTypeResponseDto toResponseDto(ActivityType activityType);
}
