package sogang.cnu.backend.lecture_room_schedule.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder
public class LectureRoomScheduleCreateCommand {
    private Quarter quarter;
    private DayOfWeek dayOfWeek;
    private LocalTime timeSlot;
    private User user;
}
