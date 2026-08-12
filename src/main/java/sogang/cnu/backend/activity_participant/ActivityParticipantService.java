package sogang.cnu.backend.activity_participant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.*;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantUpdateCommand;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantRequestDto;
import sogang.cnu.backend.activity_participant.dto.ActivityJoinRequestDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantRefundAccountDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantSummaryDto;
import sogang.cnu.backend.activity_participant.dto.ActivityCapacityResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityParticipantService {
    private static final Set<String> DEPOSIT_REQUIRED_ACTIVITY_TYPES =
            Set.of("STUDY", "SPECIAL_LECTURE");
    private static final List<ActivityParticipantStatus> CAPACITY_STATUSES =
            List.of(ActivityParticipantStatus.APPLIED, ActivityParticipantStatus.APPROVED);
    private final ActivityParticipantRepository activityParticipantRepository;
    private final ActivityParticipantMapper activityParticipantMapper;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public ActivityParticipantResponseDto getById(UUID id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));
        requireOwnerOrManager(activity);

        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantResponseDto> getAll() {
        return activityParticipantRepository.findAll().stream()
                .map(activityParticipantMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityParticipantResponseDto create(ActivityParticipantRequestDto dto) {
        Activity targetActivity = findActivityForUpdate(dto.getActivityId());
        ActivityParticipantStatus status = ActivityParticipantStatus.valueOf(dto.getStatus());
        validateAvailableCapacity(targetActivity, status);
        ActivityParticipantCreateCommand createCommand = ActivityParticipantCreateCommand.builder()
                .activity(targetActivity)
                .user(findUser(dto.getUserId()))
                .status(status)
                .build();
        ActivityParticipant activityParticipant = ActivityParticipant.create(createCommand);
        activityParticipantRepository.save(activityParticipant);
        return activityParticipantMapper.toResponseDto(activityParticipant);
    }

    @Transactional
    public ActivityParticipantResponseDto createWithUserIdAndActivityId(
            UUID userId,
            UUID activityId,
            ActivityJoinRequestDto request
    ) {
        Activity targetActivity = findActivityForUpdate(activityId);
        if (targetActivity.getStatus() != ActivityStatus.OPEN) {
            throw new ForbiddenException("현재 참여자를 모집 중인 활동이 아닙니다.");
        }
        boolean privateActivity = targetActivity.getListed() != null && !targetActivity.getListed();
        if (privateActivity &&
                !targetActivity.getAssignee().getId().equals(userId) &&
                !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("개인 프로젝트에는 참여를 신청할 수 없습니다.");
        }

        ActivityParticipantStatus initialStatus = hasStarted(targetActivity)
                ? ActivityParticipantStatus.APPROVED
                : ActivityParticipantStatus.APPLIED;

        ActivityParticipant existing = activityParticipantRepository
                .findByUserIdAndActivityId(userId, activityId)
                .orElse(null);
        if (existing != null) {
            if (existing.getStatus() != ActivityParticipantStatus.REJECTED) {
                throw new BadRequestException(
                        existing.getStatus() == ActivityParticipantStatus.APPROVED
                                ? "이미 참여가 확정된 활동입니다."
                                : "이미 참여를 신청한 활동입니다."
                );
            }
            validateAvailableCapacity(targetActivity, initialStatus);
            existing.updateStatus(initialStatus);
            if (requiresDeposit(targetActivity)) {
                recordDepositApplication(existing, request);
            }
            return activityParticipantMapper.toResponseDto(existing);
        }

        validateAvailableCapacity(targetActivity, initialStatus);
        ActivityParticipantCreateCommand createCommand = ActivityParticipantCreateCommand.builder()
                .activity(targetActivity)
                .user(findUser(userId))
                .status(initialStatus)
                .build();
        ActivityParticipant activityParticipant = ActivityParticipant.create(createCommand);
        if (requiresDeposit(targetActivity)) {
            recordDepositApplication(activityParticipant, request);
        }
        activityParticipantRepository.save(activityParticipant);
        return activityParticipantMapper.toResponseDto(activityParticipant);
    }

    @Transactional(readOnly = true)
    public ActivityCapacityResponseDto getCapacity(UUID activityId) {
        Activity activity = findActivity(activityId);
        long participantCount = countCapacityParticipants(activity);
        Integer participantLimit = activity.getParticipantLimit();
        return ActivityCapacityResponseDto.builder()
                .participantLimit(participantLimit)
                .participantCount(participantCount)
                .full(participantLimit != null && participantCount >= participantLimit)
                .build();
    }

    @Transactional
    public int confirmParticipantsForStartedActivities(LocalDate today) {
        List<ActivityParticipant> participants = activityParticipantRepository
                .findReadyForConfirmation(ActivityParticipantStatus.APPLIED, today);
        participants.forEach(ActivityParticipant::confirmOnActivityStart);
        return participants.size();
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantRefundAccountDto> getRefundAccountsByActivityId(UUID activityId) {
        findActivity(activityId);
        return activityParticipantRepository.findByActivityId(activityId).stream()
                .filter(participant -> participant.getRefundAccountNumber() != null)
                .map(participant -> ActivityParticipantRefundAccountDto.builder()
                        .participantId(participant.getId())
                        .userId(participant.getUser().getId())
                        .userName(participant.getUser().getName())
                        .studentId(participant.getUser().getStudentId())
                        .bankName(participant.getRefundBankName())
                        .accountNumber(participant.getRefundAccountNumber())
                        .accountHolder(participant.getRefundAccountHolder())
                        .paymentConfirmedAt(participant.getDepositPaymentConfirmedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityParticipantResponseDto update(UUID id, ActivityParticipantRequestDto dto) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        ActivityParticipantStatus newStatus = ActivityParticipantStatus.valueOf(dto.getStatus());
        validateCapacityTransition(activity, newStatus);
        activity.update(toUpdateCommand(dto));
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public ActivityParticipantResponseDto updateStatus(UUID id, ActivityParticipantRequestDto dto) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        ActivityParticipantStatus newStatus = ActivityParticipantStatus.valueOf(dto.getStatus());
        validateCapacityTransition(activity, newStatus);
        activity.updateStatus(newStatus);
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public ActivityParticipantResponseDto updateCompleted(UUID id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        activity.updateCompleted();
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public void delete(UUID id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));
        requireOwnerOrManager(activity);
        activityParticipantRepository.delete(activity);
    }

    // 본인의 참가 기록이거나 MANAGER/ADMIN이어야 조회·삭제(참가 취소)할 수 있다.
    private void requireOwnerOrManager(ActivityParticipant participant) {
        boolean isOwner = participant.getUser() != null
                && participant.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        if (!isOwner && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인의 참가 정보만 확인할 수 있습니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantResponseDto> getByActivityId(UUID activityId) {
        Activity activity = findActivity(activityId);
        boolean managerOrAdmin = SecurityUtils.isManagerOrAdmin();
        boolean assignee = activity.getAssignee() != null
                && activity.getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        if (!assignee && !managerOrAdmin) {
            throw new ForbiddenException("해당 활동의 담당자만 참여자 명단을 확인할 수 있습니다.");
        }
        List<ActivityParticipant> participants = activityParticipantRepository.findByActivityId(activityId);
        return participants.stream()
                .map(activityParticipantMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantSummaryDto> getVisibleMembers(UUID activityId) {
        Activity activity = findActivity(activityId);
        boolean managerOrAdmin = SecurityUtils.isManagerOrAdmin();
        boolean assignee = activity.getAssignee() != null
                && activity.getAssignee().getId().equals(SecurityUtils.getCurrentUserId());
        boolean recruitmentClosed = activity.getStatus() == ActivityStatus.ONGOING
                || activity.getStatus() == ActivityStatus.COMPLETED;

        if (!recruitmentClosed && !assignee && !managerOrAdmin) {
            throw new ForbiddenException("모집 마감 후 참여자 명단을 확인할 수 있습니다.");
        }

        return activityParticipantRepository.findByActivityId(activityId).stream()
                .filter(participant -> participant.getStatus() == ActivityParticipantStatus.APPROVED)
                .map(participant -> ActivityParticipantSummaryDto.builder()
                        .id(participant.getId())
                        .name(participant.getUser().getName() != null
                                ? participant.getUser().getName()
                                : participant.getUser().getUsername())
                        .build())
                .collect(Collectors.toList());
    }

    public ActivityParticipantResponseDto getByUserIdAndActivityId(UUID userId, UUID activityId) {
        ActivityParticipant participant = activityParticipantRepository.findByUserIdAndActivityId(userId, activityId).orElse(null);
        return activityParticipantMapper.toResponseDto(participant);
    }

    public List<ActivityParticipantResponseDto> getByUserId(UUID userId) {
        List<ActivityParticipant> participants = activityParticipantRepository.findByUserId(userId);
        return participants.stream()
                .map(activityParticipantMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private Activity findActivity(UUID activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private Activity findActivityForUpdate(UUID activityId) {
        return activityRepository.findByIdForUpdate(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private boolean requiresDeposit(Activity activity) {
        return activity.getActivityType() != null
                && DEPOSIT_REQUIRED_ACTIVITY_TYPES.contains(activity.getActivityType().getCode())
                && activity.getDepositAmount() > 0;
    }

    private boolean hasStarted(Activity activity) {
        return activity.getStartDate() != null
                && !activity.getStartDate().isAfter(LocalDate.now());
    }

    private void validateAvailableCapacity(
            Activity activity,
            ActivityParticipantStatus requestedStatus
    ) {
        if (!CAPACITY_STATUSES.contains(requestedStatus)) return;
        Integer participantLimit = activity.getParticipantLimit();
        if (participantLimit != null
                && countCapacityParticipants(activity) >= participantLimit) {
            throw new BadRequestException("참여 신청이 마감되었습니다. 정원이 모두 찼습니다.");
        }
    }

    private void validateCapacityTransition(
            ActivityParticipant participant,
            ActivityParticipantStatus newStatus
    ) {
        if (CAPACITY_STATUSES.contains(participant.getStatus())
                || !CAPACITY_STATUSES.contains(newStatus)) {
            return;
        }
        Activity activity = findActivityForUpdate(participant.getActivity().getId());
        validateAvailableCapacity(activity, newStatus);
    }

    private long countCapacityParticipants(Activity activity) {
        return activityParticipantRepository.countCapacityParticipants(
                activity,
                CAPACITY_STATUSES
        );
    }

    private void recordDepositApplication(
            ActivityParticipant participant,
            ActivityJoinRequestDto request
    ) {
        if (request == null
                || !Boolean.TRUE.equals(request.getAgreedToDepositPolicy())
                || !Boolean.TRUE.equals(request.getConfirmedDepositPayment())) {
            throw new BadRequestException("보증금 유의사항을 확인하고 필수 항목에 동의해주세요.");
        }

        String bankName = requireText(request.getRefundBankName(), "은행", 50);
        String accountHolder = requireText(request.getRefundAccountHolder(), "예금주", 50);
        String accountNumber = request.getRefundAccountNumber() == null
                ? ""
                : request.getRefundAccountNumber().replaceAll("[^0-9]", "");
        if (accountNumber.length() < 8 || accountNumber.length() > 20) {
            throw new BadRequestException("환급 계좌번호를 8~20자리 숫자로 입력해주세요.");
        }

        participant.recordDepositApplication(
                bankName,
                accountNumber,
                accountHolder,
                Boolean.TRUE.equals(request.getAgreedToPromotion())
        );
    }

    private String requireText(String value, String fieldName, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BadRequestException(fieldName + "을(를) 입력해주세요.");
        }
        if (normalized.length() > maxLength) {
            throw new BadRequestException(fieldName + "은(는) " + maxLength + "자 이하로 입력해주세요.");
        }
        return normalized;
    }

    private ActivityParticipantUpdateCommand toUpdateCommand(ActivityParticipantRequestDto dto) {
        return ActivityParticipantUpdateCommand.builder()
                .status(ActivityParticipantStatus.valueOf(dto.getStatus()))
                .build();
    }

}
