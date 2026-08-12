package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;

import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.attendance.AttendanceRepository;
import sogang.cnu.backend.attendance_report.AttendanceReportRepository;
import sogang.cnu.backend.course_time_reservation.CourseTimeReservationRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private static final String MANAGER_ONLY_ACTIVITY_TYPE = "SPECIAL_LECTURE";
    private static final int MAX_DEPOSIT_AMOUNT = 1_000_000;
    private static final int MAX_PARTICIPANT_LIMIT = 1_000;

    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final QuarterRepository quarterRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceReportRepository attendanceReportRepository;
    private final CourseTimeReservationRepository courseTimeReservationRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public ActivityResponseDto getById(UUID userId, UUID id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        if (!isListed(activity) &&
                !activity.getAssignee().getId().equals(userId) &&
                !SecurityUtils.isManagerOrAdmin()) {
            throw new NotFoundException("Activity not found");
        }

        return activityMapper.toResponseDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> getAll() {
        return activityRepository.findAll().stream()
                .filter(this::isListed)
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> getHostedByUserId(UUID userId) {
        return activityRepository.findByAssigneeId(userId).stream()
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityResponseDto create(ActivityRequestDto dto) {
        validateDepositAmount(dto.getDepositAmount());
        validateParticipantLimit(dto.getParticipantLimit());
        ActivityCreateCommand createCommand = toCreateCommand(dto);
        Activity activity = Activity.create(createCommand);
        Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toResponseDto(savedActivity);
    }

    @Transactional
    public ActivityResponseDto update(UUID userId, UUID id, ActivityRequestDto dto) {
        Activity activity = activityRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        checkPermission(userId, activity);
        ActivityType activityType = findActivityType(dto.getActivityTypeId());
        validateActivityTypeChange(activity, activityType);
        Integer depositAmount = resolveDepositAmount(activity, activityType, dto.getDepositAmount());
        Integer participantLimit = participantLimitForType(
                activityType,
                dto.getParticipantLimit()
        );
        validateParticipantLimit(participantLimit);
        validateParticipantLimitAgainstCurrentCount(activity, participantLimit);
        activity.update(toUpdateCommand(dto, activityType, depositAmount, participantLimit));
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
    public List<ActivityResponseDto> search(ActivitySearchQuery query, boolean includeUnlisted, UUID userId) {
        return activityRepository.search(query).stream()
                .filter(activity -> isVisibleInSearch(activity, includeUnlisted, userId))
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private boolean isVisibleInSearch(Activity activity, boolean includeUnlisted, UUID userId) {
        if (isListed(activity)) return true;
        if (!includeUnlisted) return false;
        return SecurityUtils.isManagerOrAdmin() || activity.getAssignee().getId().equals(userId);
    }

    private boolean isListed(Activity activity) {
        return activity.getListed() == null || Boolean.TRUE.equals(activity.getListed());
    }

    private void checkPermission(UUID userId, Activity activity) {
        boolean isAssignee = activity.getAssignee().getId().equals(userId);
        if (isAssignee) return;
        permissionChecker.checkManagerOrAdmin(userId);
    }

    private void validateActivityTypeChange(Activity activity, ActivityType requestedType) {
        boolean changesDepositPolicy = isDepositType(activity.getActivityType())
                != isDepositType(requestedType);
        if (changesDepositPolicy && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("보증금 적용 유형은 관리자 또는 운영자만 변경할 수 있습니다.");
        }
        boolean changingToManagerOnlyType = MANAGER_ONLY_ACTIVITY_TYPE.equals(requestedType.getCode())
                && !MANAGER_ONLY_ACTIVITY_TYPE.equals(activity.getActivityType().getCode());
        if (changingToManagerOnlyType && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("강의 유형은 관리자 또는 운영자만 지정할 수 있습니다.");
        }
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
        ActivityType activityType = findActivityType(dto.getActivityTypeId());
        return ActivityCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(activityType)
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .parentActivity(findParentActivity(dto.getParentActivityId()))
                .depositAmount(depositAmountForType(
                        activityType,
                        dto.getDepositAmount()
                ))
                .participantLimit(participantLimitForType(
                        activityType,
                        dto.getParticipantLimit()
                ))
                .build();
    }

    private ActivityUpdateCommand toUpdateCommand(
            ActivityRequestDto dto,
            ActivityType activityType,
            Integer depositAmount,
            Integer participantLimit
    ) {
        return ActivityUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(activityType)
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .parentActivity(findParentActivity(dto.getParentActivityId()))
                .depositAmount(depositAmount)
                .participantLimit(participantLimit)
                .build();
    }

    private Integer resolveDepositAmount(
            Activity activity,
            ActivityType requestedType,
            Integer requestedAmount
    ) {
        if (!SecurityUtils.isManagerOrAdmin()) {
            return depositAmountForType(requestedType, activity.getDepositAmount());
        }
        validateDepositAmount(requestedAmount);
        return depositAmountForType(requestedType, requestedAmount);
    }

    private Integer depositAmountForType(ActivityType activityType, Integer amount) {
        if (!isDepositType(activityType)) return 0;
        return amount == null ? 30_000 : amount;
    }

    private boolean isDepositType(ActivityType activityType) {
        String code = activityType.getCode();
        return "STUDY".equals(code) || "SPECIAL_LECTURE".equals(code);
    }

    private void validateDepositAmount(Integer amount) {
        if (amount != null && (amount < 0 || amount > MAX_DEPOSIT_AMOUNT)) {
            throw new BadRequestException("참여 보증금은 0원 이상 1,000,000원 이하로 설정해주세요.");
        }
    }

    private Integer participantLimitForType(ActivityType activityType, Integer limit) {
        if (limit != null) return limit;
        return "LECTURE".equals(activityType.getCode()) ? 5 : null;
    }

    private void validateParticipantLimit(Integer limit) {
        if (limit != null && (limit < 1 || limit > MAX_PARTICIPANT_LIMIT)) {
            throw new BadRequestException("참여 정원은 1명 이상 1,000명 이하로 설정해주세요.");
        }
    }

    private void validateParticipantLimitAgainstCurrentCount(Activity activity, Integer limit) {
        if (limit == null) return;
        long currentCount = activityParticipantRepository.countCapacityParticipants(
                activity,
                List.of(ActivityParticipantStatus.APPLIED, ActivityParticipantStatus.APPROVED)
        );
        if (limit < currentCount) {
            throw new BadRequestException("참여 정원을 현재 신청·참여 인원보다 적게 설정할 수 없습니다.");
        }
    }

}
