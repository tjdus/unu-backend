package sogang.cnu.backend.activity_session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_session.command.ActivitySessionCreateCommand;
import sogang.cnu.backend.activity_session.command.ActivitySessionUpdateCommand;
import sogang.cnu.backend.activity_session.dto.ActivitySessionRequestDto;
import sogang.cnu.backend.activity_session.dto.ActivitySessionBulkRequestDto;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;
import sogang.cnu.backend.attendance.AttendanceRepository;
import sogang.cnu.backend.attendance_report.AttendanceReportRepository;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivitySessionService {
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivitySessionMapper activitySessionMapper;
    private final ActivityRepository activityRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceReportRepository attendanceReportRepository;

    @Transactional(readOnly = true)
    public ActivitySessionResponseDto getById(UUID id) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));

        return activitySessionMapper.toResponseDto(activitySession);
    }

    @Transactional(readOnly = true)
    public List<ActivitySessionResponseDto> getAll() {
        return activitySessionRepository.findAll().stream()
                .map(activitySessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivitySessionResponseDto create(ActivitySessionRequestDto dto) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new NotFoundException("Activity not found"));
        requireScheduleManager(activity);
        validateSession(activity, dto.getSessionNumber(), dto.getDate(), null);

        ActivitySessionCreateCommand createCommand = ActivitySessionCreateCommand.builder()
                .activity(activity)
                .sessionNumber(dto.getSessionNumber())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();

        ActivitySession activitySession = ActivitySession.create(createCommand);
        ActivitySession savedActivitySession = activitySessionRepository.save(activitySession);
        return activitySessionMapper.toResponseDto(savedActivitySession);
    }

    @Transactional
    public List<ActivitySessionResponseDto> createBulk(ActivitySessionBulkRequestDto dto) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new NotFoundException("Activity not found"));
        requireScheduleManager(activity);

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("일정 종료일은 시작일 이후여야 합니다.");
        }
        validateDateInActivity(activity, dto.getStartDate());
        validateDateInActivity(activity, dto.getEndDate());

        List<ActivitySession> existing = activitySessionRepository.findByActivityId(activity.getId());
        Set<LocalDate> existingDates = existing.stream()
                .map(ActivitySession::getDate)
                .collect(java.util.stream.Collectors.toSet());
        int nextSessionNumber = existing.stream()
                .map(ActivitySession::getSessionNumber)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        Set<LocalDate> excludedDates = dto.getExcludedDates() == null
                ? Set.of()
                : new HashSet<>(dto.getExcludedDates());
        Set<DayOfWeek> weekdays = dto.getWeekdays();
        LocalDate firstWeek = dto.getStartDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<ActivitySession> sessions = new ArrayList<>();
        for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {
            LocalDate dateWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            long weekOffset = ChronoUnit.WEEKS.between(firstWeek, dateWeek);
            boolean selectedWeek = weekOffset % dto.getIntervalWeeks() == 0;
            if (!selectedWeek || !weekdays.contains(date.getDayOfWeek()) ||
                    excludedDates.contains(date) || existingDates.contains(date)) {
                continue;
            }
            if (sessions.size() >= 100) {
                throw new BadRequestException("일정은 한 번에 100개까지 생성할 수 있습니다.");
            }
            sessions.add(ActivitySession.create(ActivitySessionCreateCommand.builder()
                    .activity(activity)
                    .sessionNumber(nextSessionNumber++)
                    .date(date)
                    .description(normalizeDescription(dto.getDescription()))
                    .build()));
        }

        if (sessions.isEmpty()) {
            throw new BadRequestException("조건에 맞는 새로운 일정이 없습니다.");
        }

        return activitySessionRepository.saveAll(sessions).stream()
                .sorted(Comparator.comparing(ActivitySession::getSessionNumber))
                .map(activitySessionMapper::toResponseDto)
                .toList();
    }


    @Transactional
    public ActivitySessionResponseDto update(UUID id, ActivitySessionRequestDto dto) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));
        requireScheduleManager(activitySession.getActivity());
        if (!activitySession.getActivity().getId().equals(dto.getActivityId())) {
            throw new BadRequestException("회차의 활동은 변경할 수 없습니다.");
        }
        validateSession(
                activitySession.getActivity(),
                dto.getSessionNumber(),
                dto.getDate(),
                activitySession.getId()
        );
        ActivitySessionUpdateCommand updateCommand = ActivitySessionUpdateCommand.builder()
                .sessionNumber(dto.getSessionNumber())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();
        activitySession.update(updateCommand);
        return activitySessionMapper.toResponseDto(activitySession);
    }

    @Transactional
    public void delete(UUID id) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));
        requireScheduleManager(activitySession.getActivity());
        attendanceReportRepository.deleteBySessionId(id);
        attendanceRepository.deleteBySessionId(id);
        activitySessionRepository.delete(activitySession);
    }

    @Transactional(readOnly = true)
    public List<ActivitySessionResponseDto> getByActivityId(UUID activityId) {
        return activitySessionRepository.findByActivityId(activityId).stream()
                .map(activitySessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void validateSession(Activity activity, Integer sessionNumber, LocalDate date, UUID currentSessionId) {
        validateDateInActivity(activity, date);
        boolean duplicateNumber = currentSessionId == null
                ? activitySessionRepository.existsByActivityIdAndSessionNumber(activity.getId(), sessionNumber)
                : activitySessionRepository.existsByActivityIdAndSessionNumberAndIdNot(activity.getId(), sessionNumber, currentSessionId);
        if (duplicateNumber) {
            throw new BadRequestException("이미 등록된 회차 번호입니다.");
        }
        boolean duplicateDate = currentSessionId == null
                ? activitySessionRepository.existsByActivityIdAndDate(activity.getId(), date)
                : activitySessionRepository.existsByActivityIdAndDateAndIdNot(activity.getId(), date, currentSessionId);
        if (duplicateDate) {
            throw new BadRequestException("해당 날짜에는 이미 일정이 등록되어 있습니다.");
        }
    }

    private void validateDateInActivity(Activity activity, LocalDate date) {
        if (date.isBefore(activity.getStartDate()) || date.isAfter(activity.getEndDate())) {
            throw new BadRequestException("일정은 활동 기간 안에 등록해주세요.");
        }
    }

    private void requireScheduleManager(Activity activity) {
        boolean assignee = activity.getAssignee() != null &&
                activity.getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        if (!assignee && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("해당 활동의 담당자만 일정을 관리할 수 있습니다.");
        }
    }

    private String normalizeDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }
}
