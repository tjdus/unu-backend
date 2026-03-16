package sogang.cnu.backend.lecture_room_schedule.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class LectureRoomScheduleRequestDto {
    private UUID quarterId;
    private String dayOfWeek;
    private LocalTime timeSlot;
    private UUID userId;
}
