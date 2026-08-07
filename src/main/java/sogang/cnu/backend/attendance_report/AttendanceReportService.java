package sogang.cnu.backend.attendance_report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.activity_session.ActivitySessionRepository;
import sogang.cnu.backend.attendance.Attendance;
import sogang.cnu.backend.attendance.AttendanceRepository;
import sogang.cnu.backend.attendance.AttendanceStatus;
import sogang.cnu.backend.attendance.command.AttendanceCreateCommand;
import sogang.cnu.backend.attendance.command.AttendanceUpdateCommand;
import sogang.cnu.backend.attendance_report.command.AttendanceReportCreateCommand;
import sogang.cnu.backend.attendance_report.dto.AttendanceReportRequestDto;
import sogang.cnu.backend.attendance_report.dto.AttendanceReportResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttendanceReportService {

    private final AttendanceReportRepository attendanceReportRepository;
    private final AttendanceReportMapper attendanceReportMapper;
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional(readOnly = true)
    public AttendanceReportResponseDto getByAttendanceId(UUID attendanceId) {
        AttendanceReport report = attendanceReportRepository.findByAttendanceId(attendanceId)
                .orElseThrow(() -> new NotFoundException("Attendance report not found"));
        return attendanceReportMapper.toResponseDto(report);
    }

    @Transactional
    public AttendanceReportResponseDto submitReport(AttendanceReportRequestDto dto) {
        ActivitySession session = activitySessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new NotFoundException("Activity session not found"));

        ActivityParticipant participant = activityParticipantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Activity participant not found"));

        // 본인 참가에 대해서만 보고서를 제출할 수 있다(운영진은 대리 제출 허용).
        boolean isOwner = participant.getUser() != null
                && participant.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        if (!isOwner && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인의 참가에 대해서만 보고서를 제출할 수 있습니다.");
        }

        AttendanceStatus status = resolveStatus(session.getDate());

        Attendance attendance = attendanceRepository
                .findBySessionIdAndParticipantId(dto.getSessionId(), dto.getParticipantId())
                .map(existing -> {
                    existing.update(AttendanceUpdateCommand.builder()
                            .status(status)
                            .build());
                    return existing;
                })
                .orElseGet(() -> {
                    Attendance newAttendance = Attendance.create(AttendanceCreateCommand.builder()
                            .session(session)
                            .participant(participant)
                            .status(status)
                            .build());
                    return attendanceRepository.save(newAttendance);
                });

        attendanceReportRepository.findByAttendanceId(attendance.getId()).ifPresent(r -> {
            throw new BadRequestException("Report already submitted");
        });

        AttendanceReport report = AttendanceReport.create(AttendanceReportCreateCommand.builder()
                .attendance(attendance)
                .title(dto.getTitle())
                .content(dto.getContent())
                .build());

        return attendanceReportMapper.toResponseDto(attendanceReportRepository.save(report));
    }

    private AttendanceStatus resolveStatus(LocalDate sessionDate) {
        long daysLate = ChronoUnit.DAYS.between(sessionDate, LocalDate.now());
        if (daysLate <= 0) return AttendanceStatus.PRESENT;
        if (daysLate <= 2) return AttendanceStatus.LATE;
        return AttendanceStatus.ABSENT;
    }
}
