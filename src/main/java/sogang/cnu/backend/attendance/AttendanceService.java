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
import sogang.cnu.backend.attendance.dto.AttendanceRequestDto;
import sogang.cnu.backend.attendance.dto.AttendanceResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.user.UserRepository;

import java.util.List;
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

}
