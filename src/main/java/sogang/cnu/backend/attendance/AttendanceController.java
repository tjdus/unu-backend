package sogang.cnu.backend.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.attendance.dto.AttendanceBulkRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.attendance.dto.AttendanceStatsResponseDto;

import java.util.List;
import java.util.UUID;

// 출석 데이터는 수료 실적에 반영되므로 생성·수정·삭제와 전체/세션 단위 조회는 운영진만 가능해야 한다.
// 단, 학회원 홈에서 본인 출석 통계(stats)를 보여주므로 그 조회만 인증된 사용자에게 열어둔다.
@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceResponseDto>> getAll() {
        return ResponseEntity.ok(attendanceService.getAll());
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AttendanceResponseDto> create(@RequestBody AttendanceRequestDto attendanceRequestDto) {
        return ResponseEntity.ok(attendanceService.create(attendanceRequestDto));
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AttendanceResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AttendanceResponseDto> update(@PathVariable UUID id, @RequestBody AttendanceRequestDto attendanceRequestDto) {
        return ResponseEntity.ok(attendanceService.update(id, attendanceRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceResponseDto>> getBySessionId(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.getBySessionId(id));
    }

    @GetMapping("/participants/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceResponseDto>> getByParticipantId(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.getByParticipantId(id));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceResponseDto>> bulkCreate(@RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto) {
        return ResponseEntity.ok(attendanceService.bulkCreate(attendanceBulkRequestDto));
    }

    @PatchMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AttendanceResponseDto>> bulkUpdate(@RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto) {
        return ResponseEntity.ok(attendanceService.bulkUpdate(attendanceBulkRequestDto));
    }

    // 학회원 본인 홈 화면에서 자기 출석 통계를 보여주므로 인증된 사용자면 조회 가능하게 둔다(집계 수치만 반환).
    @GetMapping("/stats/participants/{id}")
    public ResponseEntity<AttendanceStatsResponseDto> getAttendanceStatsByParticipantId(@PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.countStatusParticipantId(id));
    }
}
