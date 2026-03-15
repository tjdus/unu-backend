package sogang.cnu.backend.lecture_room_schedule.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class LectureRoomScheduleResponseDto {
    private UUID id;
    private UUID quarterId;
    private String quarterName;
    private String dayOfWeek;
    private LocalTime timeSlot;
    private UUID userId;
    private String userName;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
}
