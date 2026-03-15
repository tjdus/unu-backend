package sogang.cnu.backend.lecture_room_schedule;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleResponseDto;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface LectureRoomScheduleMapper {

    @Mapping(source = "quarter.id", target = "quarterId")
    @Mapping(source = "quarter.name", target = "quarterName")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "dayOfWeek", target = "dayOfWeek")
    LectureRoomScheduleResponseDto toResponseDto(LectureRoomSchedule lectureRoomSchedule);
}
