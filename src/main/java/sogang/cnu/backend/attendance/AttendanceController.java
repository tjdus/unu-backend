package sogang.cnu.backend.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.attendance.dto.AttendanceBulkRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.attendance.dto.AttendanceStatsResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping("")
    public ResponseEntity<List<AttendanceResponseDto>> getAll() {
        return ResponseEntity.ok(attendanceService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<AttendanceResponseDto> create(@RequestBody AttendanceRequestDto attendanceRequestDto) {
        return ResponseEntity.ok(attendanceService.create(attendanceRequestDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> update(@PathVariable Long id, @RequestBody AttendanceRequestDto attendanceRequestDto) {
        return ResponseEntity.ok(attendanceService.update(id, attendanceRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sessions/{id}")
    public ResponseEntity<List<AttendanceResponseDto>> getBySessionId(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getBySessionId(id));
    }

    @GetMapping("/participants/{id}")
    public ResponseEntity<List<AttendanceResponseDto>> getByParticipantId(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getByParticipantId(id));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<AttendanceResponseDto>> bulkCreate(@RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto) {
        return ResponseEntity.ok(attendanceService.bulkCreate(attendanceBulkRequestDto));
    }

    @PatchMapping("/bulk")
    public ResponseEntity<List<AttendanceResponseDto>> bulkUpdate(@RequestBody AttendanceBulkRequestDto attendanceBulkRequestDto) {
        return ResponseEntity.ok(attendanceService.bulkUpdate(attendanceBulkRequestDto));
    }

    @GetMapping("/stats/participants/{id}")
    public ResponseEntity<AttendanceStatsResponseDto> getAttendanceStatsByParticipantId(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.countStatusParticipantId(id));
    }
}
