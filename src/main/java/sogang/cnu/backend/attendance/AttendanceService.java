package sogang.cnu.backend.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.activity_session.ActivitySessionRepository;
import sogang.cnu.backend.attendance.command.AttendanceCreateCommand;
import sogang.cnu.backend.attendance.command.AttendanceUpdateCommand;
import sogang.cnu.backend.attendance.dto.AttendanceBulkRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.attendance.dto.AttendanceStatsResponseDto;
import sogang.cnu.backend.attendance_report.AttendanceReportRepository;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final AttendanceReportRepository attendanceReportRepository;

    @Transactional(readOnly = true)
    public AttendanceResponseDto getById(UUID id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
        requireParticipantAccess(attendance.getParticipant());

        return attendanceMapper.toResponseDto(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAll() {
        return attendanceRepository.findAll().stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponseDto create(AttendanceRequestDto dto) {
        ActivitySession session = activitySessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new NotFoundException("Activity session not found"));
        ActivityParticipant activityParticipant = activityParticipantRepository.findById(dto.getParticipantId())
                .orElseThrow(() -> new NotFoundException("Activity participant not found"));
        requireAttendanceManager(session);
        validateParticipantForSession(session, activityParticipant);

        if (attendanceRepository.findBySessionIdAndParticipantId(session.getId(), activityParticipant.getId()).isPresent()) {
            throw new BadRequestException("이미 등록된 출석 기록입니다.");
        }

        AttendanceCreateCommand createCommand = AttendanceCreateCommand.builder()
                .session(session)
                .participant(activityParticipant)
                .status(parseManagedStatus(dto.getStatus()))
                .build();
        Attendance attendance = Attendance.create(createCommand);
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponseDto(savedAttendance);
    }

    @Transactional
    public AttendanceResponseDto update(UUID id, AttendanceRequestDto dto) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
        requireAttendanceManager(attendance.getSession());
        AttendanceUpdateCommand updateCommand = AttendanceUpdateCommand.builder()
                .status(parseManagedStatus(dto.getStatus()))
                .build();

        attendance.update(updateCommand);
        return attendanceMapper.toResponseDto(attendance);
    }

    @Transactional
    public void delete(UUID id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
        requireAttendanceManager(attendance.getSession());
        attendanceReportRepository.deleteByAttendanceId(id);
        attendanceRepository.delete(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getBySessionId(UUID sessionId) {
        ActivitySession session = activitySessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Activity session not found"));
        requireAttendanceManager(session);
        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getByParticipantId(UUID participantId) {
        ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Activity participant not found"));
        requireParticipantAccess(participant);
        return attendanceRepository.findByParticipantId(participantId).stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AttendanceResponseDto> bulkCreate(AttendanceBulkRequestDto dto) {
        return saveBulk(dto);
    }

    @Transactional
    public List<AttendanceResponseDto> bulkUpdate(AttendanceBulkRequestDto dto) {
        return saveBulk(dto);
    }

    private List<AttendanceResponseDto> saveBulk(AttendanceBulkRequestDto dto) {
        ActivitySession session = activitySessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new NotFoundException("Activity session not found"));
        requireAttendanceManager(session);

        Set<UUID> allParticipantIds = new HashSet<>();
        List<UUID> presentIds = safeList(dto.getPresentParticipantIds());
        List<UUID> absentIds = safeList(dto.getAbsentParticipantIds());
        List<UUID> excusedIds = safeList(dto.getExcusedParticipantIds());
        addUnique(allParticipantIds, presentIds);
        addUnique(allParticipantIds, absentIds);
        addUnique(allParticipantIds, excusedIds);

        Map<UUID, ActivityParticipant> approvedParticipants = activityParticipantRepository
                .findByActivityId(session.getActivity().getId()).stream()
                .filter(participant -> participant.getStatus() == ActivityParticipantStatus.APPROVED)
                .collect(Collectors.toMap(ActivityParticipant::getId, participant -> participant));
        if (!allParticipantIds.equals(approvedParticipants.keySet())) {
            throw new BadRequestException("승인된 참여자 전원의 출석 상태를 지정해주세요.");
        }

        List<Attendance> existingAttendances = attendanceRepository.findBySessionId(dto.getSessionId());
        Map<UUID, Attendance> attendanceMap = existingAttendances.stream()
                .collect(Collectors.toMap(
                        attendance -> attendance.getParticipant().getId(),
                        attendance -> attendance
                ));
        Map<UUID, AttendanceStatus> requestedStatuses = new HashMap<>();
        presentIds.forEach(id -> requestedStatuses.put(id, AttendanceStatus.PRESENT));
        absentIds.forEach(id -> requestedStatuses.put(id, AttendanceStatus.ABSENT));
        excusedIds.forEach(id -> requestedStatuses.put(id, AttendanceStatus.EXCUSED));

        List<Attendance> saved = new ArrayList<>();
        requestedStatuses.forEach((participantId, status) -> {
            Attendance attendance = attendanceMap.get(participantId);
            if (attendance == null) {
                attendance = Attendance.create(AttendanceCreateCommand.builder()
                        .session(session)
                        .participant(approvedParticipants.get(participantId))
                        .status(status)
                        .build());
            } else {
                attendance.update(AttendanceUpdateCommand.builder().status(status).build());
            }
            saved.add(attendance);
        });

        existingAttendances.stream()
                .filter(attendance -> !requestedStatuses.containsKey(attendance.getParticipant().getId()))
                .forEach(attendance -> {
                    attendanceReportRepository.deleteByAttendanceId(attendance.getId());
                    attendanceRepository.delete(attendance);
                });

        return attendanceRepository.saveAll(saved).stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendanceStatsResponseDto countStatusParticipantId(UUID participantId) {
        ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                .orElseThrow(() -> new NotFoundException("Activity participant not found"));
        requireParticipantAccess(participant);
        LocalDate today = LocalDate.now();
        Long presentCount = countStatusThrough(participantId, AttendanceStatus.PRESENT, today);
        Long absentCount = countStatusThrough(participantId, AttendanceStatus.ABSENT, today)
                + countStatusThrough(participantId, AttendanceStatus.LATE, today);
        Long excusedCount = countStatusThrough(participantId, AttendanceStatus.EXCUSED, today);

        return AttendanceStatsResponseDto.builder()
                .presentCount(presentCount)
                .absentCount(absentCount)
                .excusedCount(excusedCount)
                .build();

    }

    private Long countStatusThrough(UUID participantId, AttendanceStatus status, LocalDate date) {
        return attendanceRepository.countByParticipantIdAndStatusAndSessionDateLessThanEqual(
                participantId,
                status,
                date
        );
    }

    private List<UUID> safeList(List<UUID> ids) {
        return ids == null ? List.of() : ids;
    }

    private void addUnique(Set<UUID> target, List<UUID> ids) {
        for (UUID id : ids) {
            if (!target.add(id)) {
                throw new BadRequestException("한 참여자에게 여러 출석 상태를 지정할 수 없습니다.");
            }
        }
    }

    private AttendanceStatus parseManagedStatus(String status) {
        try {
            AttendanceStatus parsed = AttendanceStatus.valueOf(status);
            if (parsed == AttendanceStatus.LATE) {
                throw new BadRequestException("지각 상태는 사용하지 않습니다.");
            }
            return parsed;
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new BadRequestException("올바른 출석 상태를 선택해주세요.");
        }
    }

    private void validateParticipantForSession(ActivitySession session, ActivityParticipant participant) {
        if (!session.getActivity().getId().equals(participant.getActivity().getId())) {
            throw new BadRequestException("해당 활동의 참여자만 출석 처리할 수 있습니다.");
        }
        if (participant.getStatus() != ActivityParticipantStatus.APPROVED) {
            throw new BadRequestException("참여가 확정된 학회원만 출석 처리할 수 있습니다.");
        }
    }

    private void requireAttendanceManager(ActivitySession session) {
        boolean assignee = session.getActivity().getAssignee() != null &&
                session.getActivity().getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        if (!assignee && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("해당 활동의 담당자만 출석을 관리할 수 있습니다.");
        }
    }

    private void requireParticipantAccess(ActivityParticipant participant) {
        boolean owner = participant.getUser() != null &&
                participant.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        boolean assignee = participant.getActivity().getAssignee() != null &&
                participant.getActivity().getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        if (!owner && !assignee && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인의 출석 정보만 확인할 수 있습니다.");
        }
    }

}
