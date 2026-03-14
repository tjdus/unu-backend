package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.NotFoundException;

import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.attendance.AttendanceRepository;
import sogang.cnu.backend.attendance_report.AttendanceReportRepository;
import sogang.cnu.backend.course_time_reservation.CourseTimeReservationRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final QuarterRepository quarterRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceReportRepository attendanceReportRepository;
    private final CourseTimeReservationRepository courseTimeReservationRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public ActivityResponseDto getById(UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        return activityMapper.toResponseDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> getAll() {
        return activityRepository.findAll().stream()
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityResponseDto create(ActivityRequestDto dto) {
        ActivityCreateCommand createCommand = toCreateCommand(dto);
        Activity activity = Activity.create(createCommand);
        Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toResponseDto(savedActivity);
    }

    @Transactional
    public ActivityResponseDto createWithAssignee(UUID userId, ActivityRequestDto dto) {
        dto.setAssigneeId(userId);
        dto.setStatus(String.valueOf(ActivityStatus.CREATED));
        ActivityCreateCommand createCommand = toCreateCommand(dto);
        Activity activity = Activity.create(createCommand);
        Activity savedActivity = activityRepository.save(activity);

        User assignee = findAssignee(userId);
        ActivityParticipant participant = ActivityParticipant.create(
                ActivityParticipantCreateCommand.builder()
                        .activity(savedActivity)
                        .user(assignee)
                        .status(ActivityParticipantStatus.APPLIED)
                        .build()
        );
        participant.updateStatus(ActivityParticipantStatus.APPROVED);
        activityParticipantRepository.save(participant);

        return activityMapper.toResponseDto(savedActivity);
    }

    @Transactional
    public ActivityResponseDto update(UUID userId, UUID id, ActivityRequestDto dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        checkPermission(userId, activity);
        activity.update(toUpdateCommand(dto));
        return activityMapper.toResponseDto(activity);
    }

    @Transactional
    public ActivityResponseDto updateStatus(UUID id, String status) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        activity.updateStatus(ActivityStatus.valueOf(status));
        return activityMapper.toResponseDto(activity);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        checkPermission(userId, activity);

        attendanceReportRepository.deleteByActivityId(id);
        attendanceRepository.deleteByActivityId(id);
        courseTimeReservationRepository.deleteByActivityId(id);
        activityRepository.detachChildActivities(id);
        activityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> search(ActivitySearchQuery query) {
        return activityRepository.search(query).stream()
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private void checkPermission(UUID userId, Activity activity) {
        boolean isAssignee = activity.getAssignee().getId().equals(userId);
        if (isAssignee) return;
        permissionChecker.checkManagerOrAdmin(userId);
    }

    private ActivityType findActivityType(UUID activityTypeId) {
        return activityTypeRepository.findById(activityTypeId)
                .orElseThrow(() -> new NotFoundException("Activity type not found"));
    }
    private User findAssignee(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
    }
    private Quarter findQuarter(UUID quarterId) {
        return quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));
    }

    private Activity findParentActivity(UUID parentActivityId) {
        if (parentActivityId == null) return null;
        return activityRepository.findById(parentActivityId)
                .orElseThrow(() -> new NotFoundException("Parent activity not found"));
    }

    private ActivityCreateCommand toCreateCommand(ActivityRequestDto dto) {
        return ActivityCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(findActivityType(dto.getActivityTypeId()))
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .parentActivity(findParentActivity(dto.getParentActivityId()))
                .build();
    }

    private ActivityUpdateCommand toUpdateCommand(ActivityRequestDto dto) {
        return ActivityUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(findActivityType(dto.getActivityTypeId()))
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .parentActivity(findParentActivity(dto.getParentActivityId()))
                .build();
    }

}
