package sogang.cnu.backend.lecture_room_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleRequestDto;
import sogang.cnu.backend.lecture_room_schedule.dto.LectureRoomScheduleResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lecture-room-schedules")
@RequiredArgsConstructor
public class LectureRoomScheduleController {

    private final LectureRoomScheduleService lectureRoomScheduleService;

    @GetMapping("/quarters/{quarterId}")
    public ResponseEntity<List<LectureRoomScheduleResponseDto>> getByQuarter(
            @PathVariable UUID quarterId,
            @RequestParam(required = false) String dayOfWeek) {
        if (dayOfWeek != null) {
            return ResponseEntity.ok(lectureRoomScheduleService.getByQuarterAndDay(quarterId, dayOfWeek));
        }
        return ResponseEntity.ok(lectureRoomScheduleService.getByQuarter(quarterId));
    }

    @PostMapping("")
    public ResponseEntity<LectureRoomScheduleResponseDto> create(@RequestBody LectureRoomScheduleRequestDto dto) {
        return ResponseEntity.ok(lectureRoomScheduleService.create(dto));
    }

    @PostMapping("/me")
    public ResponseEntity<LectureRoomScheduleResponseDto> createForMe(
            @CurrentUser CustomUserDetails user,
            @RequestBody LectureRoomScheduleRequestDto dto) {
        return ResponseEntity.ok(lectureRoomScheduleService.createForMe(user.getId(), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lectureRoomScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
