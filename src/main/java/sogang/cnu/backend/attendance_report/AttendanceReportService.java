package sogang.cnu.backend.attendance_report;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
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
        requireParticipantAccess(report.getAttendance().getParticipant());
        return attendanceReportMapper.toResponseDto(report);
    }

    @Transactional
    public AttendanceReportResponseDto submitReport(AttendanceReportRequestDto dto) {
        ActivitySession session = activitySessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new NotFoundException("Activity session not found"));

        ActivityParticipant participant = activityParticipantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Activity participant not found"));

        if (!session.getActivity().getId().equals(participant.getActivity().getId())) {
            throw new BadRequestException("해당 활동의 참여자만 보고서를 제출할 수 있습니다.");
        }
        if (participant.getStatus() != ActivityParticipantStatus.APPROVED) {
            throw new BadRequestException("참여가 확정된 학회원만 보고서를 제출할 수 있습니다.");
        }
        if (session.getActivity().getActivityType() == null ||
                !"ONLINE_COURSE".equals(session.getActivity().getActivityType().getCode())) {
            throw new BadRequestException("온라인 강의 활동에만 보고서를 제출할 수 있습니다.");
        }

        requireReportSubmitter(participant);

        if (session.getDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("보고서 제출 기간이 마감되었습니다.");
        }

        attendanceRepository
                .findBySessionIdAndParticipantId(dto.getSessionId(), dto.getParticipantId())
                .flatMap(attendance -> attendanceReportRepository.findByAttendanceId(attendance.getId()))
                .ifPresent(report -> {
                    throw new BadRequestException("이미 제출한 보고서입니다.");
                });

        AttendanceStatus status = AttendanceStatus.PRESENT;

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

        AttendanceReport report = AttendanceReport.create(AttendanceReportCreateCommand.builder()
                .attendance(attendance)
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .build());

        return attendanceReportMapper.toResponseDto(attendanceReportRepository.save(report));
    }

    private void requireParticipantAccess(ActivityParticipant participant) {
        boolean owner = participant.getUser() != null
                && participant.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        boolean assignee = participant.getActivity().getAssignee() != null
                && participant.getActivity().getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        if (!owner && !assignee && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("해당 보고서를 확인할 권한이 없습니다.");
        }
    }

    private void requireReportSubmitter(ActivityParticipant participant) {
        boolean owner = participant.getUser() != null
                && participant.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        if (!owner && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인의 참가에 대해서만 보고서를 제출할 수 있습니다.");
        }
    }
}
