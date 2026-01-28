package sogang.cnu.backend.activity;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;

@Mapper(componentModel = "spring")
public interface ActivityMapper {
    ActivityResponseDto toResponseDto(Activity activity);
}


