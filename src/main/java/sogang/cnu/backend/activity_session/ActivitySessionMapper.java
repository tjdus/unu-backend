package sogang.cnu.backend.activity_session;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;


@Mapper(componentModel = "spring")
public interface ActivitySessionMapper {
    ActivitySessionResponseDto toResponseDto(ActivitySession activitySession);
}

