package sogang.cnu.backend.lecture_room_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 임의 사용자(dto.userId)를 배정하는 경로. 학회실 관리 권한이 있는 사람만 가능하다.
    // 학회원 본인 예약은 아래 /me 경로를 쓴다.
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','LECTURE_ROOM_MANAGER')")
    public ResponseEntity<LectureRoomScheduleResponseDto> create(@RequestBody LectureRoomScheduleRequestDto dto) {
        return ResponseEntity.ok(lectureRoomScheduleService.create(dto));
    }

    @PostMapping("/me")
    public ResponseEntity<LectureRoomScheduleResponseDto> createForMe(
            @CurrentUser CustomUserDetails user,
            @RequestBody LectureRoomScheduleRequestDto dto) {
        return ResponseEntity.ok(lectureRoomScheduleService.createForMe(user.getId(), dto));
    }

    // 본인 예약 취소 또는 학회실 관리자가 배정 해제. 소유자/권한 확인은 서비스에서 한다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lectureRoomScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
