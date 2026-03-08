package sogang.cnu.backend.activity_participant;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface ActivityParticipantMapper {
    ActivityParticipantResponseDto toResponseDto(ActivityParticipant activityParticipant);
}
