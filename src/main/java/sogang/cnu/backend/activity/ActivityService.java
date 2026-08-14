package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;

import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.attendance.AttendanceRepository;
import sogang.cnu.backend.attendance_report.AttendanceReportRepository;
import sogang.cnu.backend.course_time_reservation.CourseTimeReservationRepository;
import sogang.cnu.backend.activity_notice.ActivityNoticeRepository;
import sogang.cnu.backend.activity_notice.ActivityNoticeReadRepository;
import sogang.cnu.backend.activity_opening_request.ActivityOpeningRequestRepository;
import sogang.cnu.backend.lecture_material.LectureMaterialRepository;
import sogang.cnu.backend.lecture_material.LectureMaterialService;
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
    private final LectureMaterialRepository lectureMaterialRepository;
    private final LectureMaterialService lectureMaterialService;
    private final ActivityNoticeRepository activityNoticeRepository;
    private final ActivityNoticeReadRepository activityNoticeReadRepository;
    private final ActivityOpeningRequestRepository activityOpeningRequestRepository;
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
        validateRecruitmentPeriod(dto);
        ActivityCreateCommand createCommand = toCreateCommand(dto);
        Activity activity = Activity.create(createCommand);
        Activity savedActivity = activityRepository.save(activity);
        lectureMaterialService.syncPrimaryMaterial(savedActivity, dto.getMaterialUrl());
        registerAssigneeAsParticipant(savedActivity);
        return activityMapper.toResponseDto(savedActivity);
    }

    @Transactional
    public ActivityResponseDto update(UUID userId, UUID id, ActivityRequestDto dto) {
        Activity activity = activityRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        checkPermission(userId, activity);
        if (!SecurityUtils.isManagerOrAdmin()) {
            activity.update(toAssigneeUpdateCommand(activity, dto));
            lectureMaterialService.syncPrimaryMaterial(activity, dto.getMaterialUrl());
            return activityMapper.toResponseDto(activity);
        }

        ActivityType activityType = findActivityType(dto.getActivityTypeId());
        validateActivityTypeChange(activity, activityType);
        Integer depositAmount = resolveDepositAmount(activity, activityType, dto.getDepositAmount());
        Integer participantLimit = participantLimitForListing(
                dto.getListed(),
                participantLimitForType(activityType, dto.getParticipantLimit())
        );
        validateParticipantLimit(participantLimit);
        validateParticipantLimitAgainstCurrentCount(activity, participantLimit);
        validateRecruitmentPeriod(dto);
        activity.update(toUpdateCommand(dto, activityType, depositAmount, participantLimit));
        lectureMaterialService.syncPrimaryMaterial(activity, dto.getMaterialUrl());
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
        lectureMaterialRepository.deleteByActivityIdAndPrimaryTrue(id);
        lectureMaterialRepository.detachFromActivity(id);
        activityNoticeReadRepository.deleteByActivityId(id);
        // 공지는 활동에 종속되므로 같이 지운다 (자료와 달리 공용으로 남길 수 없다)
        activityNoticeRepository.deleteByActivityId(id);
        // 승인으로 생성된 활동이면 연결된 개설 신청 검토 기록도 함께 지운다.
        activityOpeningRequestRepository.findByApprovedActivityId(id)
                .ifPresent(request -> {
                    activityOpeningRequestRepository.delete(request);
                    activityOpeningRequestRepository.flush();
                });
        activityOpeningRequestRepository.detachParentActivity(id);
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

    /**
     * 스터디처럼 담당자도 참여자인 유형은 개설과 동시에 참여 확정으로 등록한다.
     * 개설 신청 승인 경로에서는 신청자를 이미 등록하므로 여기선 직접 생성만 다룬다.
     */
    private void registerAssigneeAsParticipant(Activity activity) {
        if (!activity.includesAssigneeAsParticipant() || activity.getAssignee() == null) return;

        boolean alreadyRegistered = activityParticipantRepository
                .findByUserIdAndActivityId(activity.getAssignee().getId(), activity.getId())
                .isPresent();
        if (alreadyRegistered) return;

        ActivityParticipant participant = ActivityParticipant.create(
                ActivityParticipantCreateCommand.builder()
                        .activity(activity)
                        .user(activity.getAssignee())
                        .status(ActivityParticipantStatus.APPLIED)
                        .build()
        );
        participant.updateStatus(ActivityParticipantStatus.APPROVED);
        activityParticipantRepository.save(participant);
    }

    /** 모집 기간은 선택이지만, 넣는다면 개설 승인 경로와 같은 규칙을 지켜야 한다. */
    private void validateRecruitmentPeriod(ActivityRequestDto dto) {
        java.time.LocalDate start = dto.getRecruitmentStartDate();
        java.time.LocalDate end = dto.getRecruitmentEndDate();

        if (start == null && end == null) return;
        if (start == null || end == null) {
            throw new BadRequestException("모집 시작일과 종료일을 모두 입력해주세요.");
        }
        if (end.isBefore(start)) {
            throw new BadRequestException("모집 종료일은 모집 시작일보다 빠를 수 없습니다.");
        }
        if (dto.getStartDate() != null && end.isAfter(dto.getStartDate())) {
            throw new BadRequestException("모집 종료일은 활동 시작일 이후로 설정할 수 없습니다.");
        }
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
                .participantLimit(participantLimitForListing(
                        dto.getListed(),
                        participantLimitForType(activityType, dto.getParticipantLimit())
                ))
                .listed(dto.getListed())
                .recruitmentPositions(normalizeRecruitmentPositions(dto.getRecruitmentPositions()))
                .discordUrl(normalizeDiscordUrl(dto.getDiscordUrl()))
                .recruitmentStartDate(dto.getRecruitmentStartDate())
                .recruitmentEndDate(dto.getRecruitmentEndDate())
                .operationPlan(normalizeOperationPlan(activityType, dto.getOperationPlan()))
                .instructorCareer(normalizeInstructorCareer(activityType, dto.getInstructorCareer()))
                .build();
    }

    private String normalizeRecruitmentPositions(String positions) {
        if (positions == null || positions.isBlank()) return null;
        return positions.trim();
    }

    private String normalizeOperationPlan(ActivityType activityType, String operationPlan) {
        String code = activityType.getCode();
        if (!"STUDY".equals(code) && !"SPECIAL_LECTURE".equals(code)) return null;
        if (operationPlan == null || operationPlan.isBlank()) return null;
        return operationPlan.trim();
    }

    private String normalizeInstructorCareer(ActivityType activityType, String instructorCareer) {
        if (!"SPECIAL_LECTURE".equals(activityType.getCode())) return null;
        if (instructorCareer == null || instructorCareer.isBlank()) return null;
        return instructorCareer.trim();
    }

    private static final java.util.Set<String> DISCORD_HOSTS = java.util.Set.of(
            "discord.gg",
            "discord.com",
            "www.discord.com",
            "discordapp.com"
    );

    /** 선택 입력이라 비어 있으면 그냥 없는 값으로 두고, 넣었다면 디스코드 링크인지 확인한다. */
    private String normalizeDiscordUrl(String url) {
        if (url == null || url.isBlank()) return null;
        String normalized = url.trim();
        try {
            java.net.URI uri = new java.net.URI(normalized);
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme()) ||
                    host == null ||
                    !DISCORD_HOSTS.contains(host.toLowerCase())) {
                throw new BadRequestException("디스코드 초대 링크를 확인해주세요.");
            }
            return normalized;
        } catch (java.net.URISyntaxException e) {
            throw new BadRequestException("디스코드 초대 링크를 확인해주세요.");
        }
    }

    /** 목록에 공개하지 않는 활동(개인 프로젝트)은 신청을 받지 않으므로 정원 개념이 없다. */
    private Integer participantLimitForListing(Boolean listed, Integer participantLimit) {
        return Boolean.FALSE.equals(listed) ? null : participantLimit;
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
                .listed(dto.getListed())
                .recruitmentPositions(normalizeRecruitmentPositions(dto.getRecruitmentPositions()))
                .discordUrl(normalizeDiscordUrl(dto.getDiscordUrl()))
                .recruitmentStartDate(dto.getRecruitmentStartDate())
                .recruitmentEndDate(dto.getRecruitmentEndDate())
                .operationPlan(normalizeOperationPlan(activityType, dto.getOperationPlan()))
                .instructorCareer(normalizeInstructorCareer(activityType, dto.getInstructorCareer()))
                .build();
    }

    private ActivityUpdateCommand toAssigneeUpdateCommand(
            Activity activity,
            ActivityRequestDto dto
    ) {
        ActivityType activityType = activity.getActivityType();
        return ActivityUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(activity.getStatus())
                .activityType(activityType)
                .assignee(activity.getAssignee())
                .quarter(activity.getQuarter())
                .startDate(activity.getStartDate())
                .endDate(activity.getEndDate())
                .parentActivity(activity.getParentActivity())
                .depositAmount(activity.getDepositAmount())
                .participantLimit(activity.getParticipantLimit())
                .listed(activity.getListed())
                .recruitmentPositions(activity.getRecruitmentPositions())
                .discordUrl(normalizeDiscordUrl(dto.getDiscordUrl()))
                .operationPlan(normalizeOperationPlan(activityType, dto.getOperationPlan()))
                .instructorCareer(normalizeInstructorCareer(activityType, dto.getInstructorCareer()))
                .recruitmentStartDate(activity.getRecruitmentStartDate())
                .recruitmentEndDate(activity.getRecruitmentEndDate())
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
