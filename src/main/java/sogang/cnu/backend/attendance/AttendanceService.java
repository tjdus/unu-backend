package sogang.cnu.backend.attendance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_session.ActivitySession;
import sogang.cnu.backend.activity_session.ActivitySessionRepository;
import sogang.cnu.backend.attendance.command.AttendanceCreateCommand;
import sogang.cnu.backend.attendance.command.AttendanceUpdateCommand;
import sogang.cnu.backend.attendance.dto.AttendanceBulkRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.user.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final UserRepository userRepository;
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivityParticipantRepository activityParticipantRepository;

    @Transactional(readOnly = true)
    public AttendanceResponseDto getById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));

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

        AttendanceCreateCommand createCommand = AttendanceCreateCommand.builder()
                .session(session)
                .participant(activityParticipant)
                .status(AttendanceStatus.valueOf(dto.getStatus()))
                .build();
        Attendance attendance = Attendance.create(createCommand);
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponseDto(savedAttendance);
    }

    @Transactional
    public AttendanceResponseDto update(Long id, AttendanceRequestDto dto) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
        AttendanceUpdateCommand updateCommand = AttendanceUpdateCommand.builder()
                .status(AttendanceStatus.valueOf(dto.getStatus()))
                .build();

        attendance.update(updateCommand);
        return attendanceMapper.toResponseDto(attendance);
    }

    @Transactional
    public void delete(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Attendance not found"));
        attendanceRepository.delete(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getBySessionId(Long sessionId) {
        return attendanceRepository.findBySessionId(sessionId).stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getByParticipantId(Long participantId) {
        return attendanceRepository.findByParticipantId(participantId).stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AttendanceResponseDto> bulkCreate(AttendanceBulkRequestDto dto) {
        // 세션 존재 확인
        ActivitySession session = activitySessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new NotFoundException("Activity session not found"));

        // 모든 participant ID 수집
        Set<Long> allParticipantIds = new HashSet<>();
        List<Long> presentIds = dto.getPresentParticipantIds() != null ? dto.getPresentParticipantIds() : new ArrayList<>();
        List<Long> absentIds = dto.getAbsentParticipantIds() != null ? dto.getAbsentParticipantIds() : new ArrayList<>();
        List<Long> excusedIds = dto.getExcusedParticipantIds() != null ? dto.getExcusedParticipantIds() : new ArrayList<>();

        // 중복 ID 체크
        for (Long id : presentIds) {
            if (!allParticipantIds.add(id)) {
                throw new BadRequestException("Duplicate participant ID found: " + id);
            }
        }
        for (Long id : absentIds) {
            if (!allParticipantIds.add(id)) {
                throw new BadRequestException("Duplicate participant ID found: " + id);
            }
        }
        for (Long id : excusedIds) {
            if (!allParticipantIds.add(id)) {
                throw new BadRequestException("Duplicate participant ID found: " + id);
            }
        }

        // Attendance 엔티티 생성
        List<Attendance> attendances = new ArrayList<>();

        // PRESENT 상태 생성
        for (Long participantId : presentIds) {
            ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                    .orElseThrow(() -> new NotFoundException("Activity participant not found: " + participantId));

            AttendanceCreateCommand command = AttendanceCreateCommand.builder()
                    .session(session)
                    .participant(participant)
                    .status(AttendanceStatus.PRESENT)
                    .build();
            attendances.add(Attendance.create(command));
        }

        // ABSENT 상태 생성
        for (Long participantId : absentIds) {
            ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                    .orElseThrow(() -> new NotFoundException("Activity participant not found: " + participantId));

            AttendanceCreateCommand command = AttendanceCreateCommand.builder()
                    .session(session)
                    .participant(participant)
                    .status(AttendanceStatus.ABSENT)
                    .build();
            attendances.add(Attendance.create(command));
        }

        // EXCUSED 상태 생성
        for (Long participantId : excusedIds) {
            ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                    .orElseThrow(() -> new NotFoundException("Activity participant not found: " + participantId));

            AttendanceCreateCommand command = AttendanceCreateCommand.builder()
                    .session(session)
                    .participant(participant)
                    .status(AttendanceStatus.EXCUSED)
                    .build();
            attendances.add(Attendance.create(command));
        }

        // 일괄 저장
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendances);

        return savedAttendances.stream()
                .map(attendanceMapper::toResponseDto)
                .collect(Collectors.toList());
    }

}
