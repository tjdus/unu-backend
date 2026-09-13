package sogang.cnu.backend.activity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.activity.dto.ActivityAssigneeResponseDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.user.User;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface ActivityMapper {
    @Mapping(target = "parentActivityId", source = "parentActivity.id")
    ActivityResponseDto toResponseDto(Activity activity);

    default ActivityAssigneeResponseDto toAssigneeResponseDto(User user) {
        if (user == null) return null;
        return ActivityAssigneeResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}

