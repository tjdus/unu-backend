package sogang.cnu.backend.lecture_room_schedule.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureRoomScheduleImportResponseDto {
    private int userCount;
    private int deletedCount;
    private int createdCount;
}
