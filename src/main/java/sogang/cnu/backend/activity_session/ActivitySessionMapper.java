package sogang.cnu.backend.activity_session;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface ActivitySessionMapper {
    ActivitySessionResponseDto toResponseDto(ActivitySession activitySession);
}

