package sogang.cnu.backend.activity_participant;

import org.mapstruct.Mapper;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;

@Mapper(componentModel = "spring")
public interface ActivityParticipantMapper {
    ActivityParticipantResponseDto toResponseDto(ActivityParticipant activityParticipant);
}
