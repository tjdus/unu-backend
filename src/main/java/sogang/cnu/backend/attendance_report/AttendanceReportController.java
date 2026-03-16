package sogang.cnu.backend.attendance_report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.attendance_report.dto.AttendanceReportRequestDto;
import sogang.cnu.backend.attendance_report.dto.AttendanceReportResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/attendance-reports")
@RequiredArgsConstructor
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;

    @GetMapping("/attendances/{attendanceId}")
    public ResponseEntity<AttendanceReportResponseDto> getByAttendanceId(@PathVariable UUID attendanceId) {
        return ResponseEntity.ok(attendanceReportService.getByAttendanceId(attendanceId));
    }

    @PostMapping("")
    public ResponseEntity<AttendanceReportResponseDto> submitReport(@RequestBody AttendanceReportRequestDto dto) {
        return ResponseEntity.ok(attendanceReportService.submitReport(dto));
    }
}
