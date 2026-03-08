package sogang.cnu.backend.activity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface ActivityMapper {
    @Mapping(target = "parentActivityId", source = "parentActivity.id")
    ActivityResponseDto toResponseDto(Activity activity);
}


