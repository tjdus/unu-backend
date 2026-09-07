package sogang.cnu.backend.activity_opening_request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity.ActivityService;
import sogang.cnu.backend.activity.ActivityStatus;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity_opening_period.ActivityOpeningPeriodService;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningRequestDto;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningRequestResponseDto;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;
import sogang.cnu.backend.lecture_material.LectureMaterialService;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.MemberStatus;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user.dto.UserSummaryResponseDto;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ActivityOpeningRequestService {
    private final ActivityOpeningRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final QuarterRepository quarterRepository;
    private final ActivityRepository activityRepository;
    private final ActivityService activityService;
    private final ActivityParticipantRepository participantRepository;
    private final ActivityOpeningPeriodService openingPeriodService;
    private final LectureMaterialService lectureMaterialService;

    @Transactional
    public ActivityOpeningRequestResponseDto createDraft(UUID applicantId, ActivityOpeningRequestDto dto) {
        return toResponse(createRequest(applicantId, dto));
    }

    @Transactional
    public ActivityOpeningRequestResponseDto createAndSubmit(UUID applicantId, ActivityOpeningRequestDto dto) {
        ActivityOpeningRequest request = createRequest(applicantId, dto);
        validateSubmissionEligibility(request);
        request.submit();
        return toResponse(request);
    }

    private ActivityOpeningRequest createRequest(UUID applicantId, ActivityOpeningRequestDto dto) {
        User applicant = findApplicant(applicantId);
        RequestReferences references = resolveReferences(applicantId, dto);
        openingPeriodService.requireApplicationOpen(references.quarter().getId());
        validateRequest(applicantId, dto, references);

        ActivityOpeningRequest request = ActivityOpeningRequest.builder()
                .applicant(applicant)
                .title(dto.getTitle().trim())
                .description(dto.getDescription().trim())
                .operationPlan(operationPlanForRequest(dto, references.activityType()))
                .materialUrl(materialUrlForRequest(dto, references.activityType()))
                .activityType(references.activityType())
                .quarter(references.quarter())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .expectedMemberCount(dto.getExpectedMemberCount())
                .acceptsNewMembers(dto.getAcceptsNewMembers())
                .participantLimit(participantLimitForRequest(dto))
                .recruitmentPositions(recruitmentPositionsForRequest(dto))
                .instructorCareer(instructorCareerForRequest(dto))
                .personalProject(dto.getPersonalProject())
                .parentActivity(references.parentActivity())
                .initialMembers(references.initialMembers())
                .status(ActivityOpeningRequestStatus.DRAFT)
                .build();

        return requestRepository.save(request);
    }

    @Transactional
    public ActivityOpeningRequestResponseDto update(UUID applicantId, UUID requestId, ActivityOpeningRequestDto dto) {
        return toResponse(updateRequest(applicantId, requestId, dto));
    }

    @Transactional
    public ActivityOpeningRequestResponseDto updateAndSubmit(
            UUID applicantId,
            UUID requestId,
            ActivityOpeningRequestDto dto
    ) {
        ActivityOpeningRequest request = updateRequest(applicantId, requestId, dto);
        validateSubmissionEligibility(request);
        request.submit();
        return toResponse(request);
    }

    private ActivityOpeningRequest updateRequest(UUID applicantId, UUID requestId, ActivityOpeningRequestDto dto) {
        ActivityOpeningRequest request = findOwnedForUpdate(requestId, applicantId);
        requireEditable(request);
        RequestReferences references = resolveReferences(applicantId, dto);
        requirePeriodOpen(request, references.quarter().getId());
        validateRequest(applicantId, dto, references);

        request.update(
                dto.getTitle().trim(),
                dto.getDescription().trim(),
                operationPlanForRequest(dto, references.activityType()),
                materialUrlForRequest(dto, references.activityType()),
                references.activityType(),
                references.quarter(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getExpectedMemberCount(),
                dto.getAcceptsNewMembers(),
                participantLimitForRequest(dto),
                recruitmentPositionsForRequest(dto),
                instructorCareerForRequest(dto),
                dto.getPersonalProject(),
                references.parentActivity(),
                references.initialMembers()
        );
        return request;
    }

    @Transactional
    public ActivityOpeningRequestResponseDto submit(UUID applicantId, UUID requestId) {
        ActivityOpeningRequest request = findOwnedForUpdate(requestId, applicantId);
        requireEditable(request);
        requirePeriodOpen(request, request.getQuarter().getId());
        validateSubmissionEligibility(request);
        request.submit();
        return toResponse(request);
    }

    @Transactional
    public ActivityOpeningRequestResponseDto cancel(UUID applicantId, UUID requestId) {
        ActivityOpeningRequest request = findOwnedForUpdate(requestId, applicantId);
        if (request.getStatus() == ActivityOpeningRequestStatus.APPROVED ||
                request.getStatus() == ActivityOpeningRequestStatus.CANCELED) {
            throw new BadRequestException("취소할 수 없는 신청입니다.");
        }
        request.cancel();
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<ActivityOpeningRequestResponseDto> getMine(UUID applicantId) {
        return requestRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityOpeningRequestResponseDto get(UUID userId, UUID requestId) {
        ActivityOpeningRequest request = find(requestId);
        if (!request.getApplicant().getId().equals(userId) && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인의 신청만 조회할 수 있습니다.");
        }
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<ActivityOpeningRequestResponseDto> getAllForManagement() {
        return requestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ActivityOpeningRequestResponseDto getForManagement(UUID requestId) {
        return toResponse(find(requestId));
    }

    @Transactional
    public ActivityOpeningRequestResponseDto review(
            UUID reviewerId,
            UUID requestId,
            ActivityOpeningRequestStatus status,
            String comment
    ) {
        if (status != ActivityOpeningRequestStatus.SUBMITTED &&
                status != ActivityOpeningRequestStatus.REVISION_REQUESTED &&
                status != ActivityOpeningRequestStatus.REJECTED) {
            throw new BadRequestException("검토 대기, 보완 요청 또는 반려 상태만 선택할 수 있습니다.");
        }
        if (status != ActivityOpeningRequestStatus.SUBMITTED &&
                (comment == null || comment.isBlank())) {
            throw new BadRequestException("검토 의견을 입력해주세요.");
        }

        ActivityOpeningRequest request = findForUpdate(requestId);
        if (request.getStatus() != ActivityOpeningRequestStatus.SUBMITTED &&
                request.getStatus() != ActivityOpeningRequestStatus.REVISION_REQUESTED &&
                request.getStatus() != ActivityOpeningRequestStatus.REJECTED &&
                request.getStatus() != ActivityOpeningRequestStatus.APPROVED) {
            throw new BadRequestException("현재 상태의 신청은 검토 상태를 변경할 수 없습니다.");
        }

        if (request.getStatus() == ActivityOpeningRequestStatus.APPROVED) {
            Activity approvedActivity = request.unlinkApprovedActivity();
            requestRepository.flush();
            if (approvedActivity != null) {
                activityService.delete(reviewerId, approvedActivity.getId());
            }
        }

        request.review(status, findUser(reviewerId), normalizeComment(comment));
        return toResponse(request);
    }

    @Transactional
    public ActivityOpeningRequestResponseDto approve(
            UUID reviewerId,
            UUID requestId,
            String comment,
            Integer depositAmount,
            LocalDate recruitmentStartDate,
            LocalDate recruitmentEndDate
    ) {
        ActivityOpeningRequest request = findForUpdate(requestId);
        if (request.getStatus() == ActivityOpeningRequestStatus.APPROVED
                && request.getApprovedActivity() != null) {
            return toResponse(request);
        }

        if (request.getStatus() != ActivityOpeningRequestStatus.SUBMITTED) {
            throw new BadRequestException("제출된 신청만 승인할 수 있습니다.");
        }

        validateSubmissionEligibility(request);

        String activityTypeCode = request.getActivityType().getCode();

        boolean usesDeposit =
                "STUDY".equals(activityTypeCode) ||
                "SPECIAL_LECTURE".equals(activityTypeCode);

        boolean recruitsMembers =
                "SPECIAL_LECTURE".equals(activityTypeCode) ||
                Boolean.TRUE.equals(request.getAcceptsNewMembers());

        if (usesDeposit && depositAmount == null) {
            throw new BadRequestException("참여 보증금을 설정해주세요.");
        }

        if (depositAmount != null && depositAmount < 0) {
            throw new BadRequestException("참여 보증금은 0원 이상이어야 합니다.");
        }

        if (recruitsMembers) {
            if (recruitmentStartDate == null || recruitmentEndDate == null) {
                throw new BadRequestException("모집 기간을 설정해주세요.");
            }

            if (recruitmentEndDate.isBefore(recruitmentStartDate)) {
                throw new BadRequestException("모집 종료일은 모집 시작일보다 빠를 수 없습니다.");
            }

            if (request.getStartDate() != null &&
                    recruitmentEndDate.isAfter(request.getStartDate())) {
                throw new BadRequestException("모집 종료일은 활동 시작일 이후로 설정할 수 없습니다.");
            }
        }

        validateApprovalParticipantCounts(request);

        Activity activity = Activity.create(
            ActivityCreateCommand.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .status(ActivityStatus.CREATED)
                    .activityType(request.getActivityType())
                    .assignee(request.getApplicant())
                    .quarter(request.getQuarter())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .recruitmentStartDate(
                            recruitsMembers ? recruitmentStartDate : null
                    )
                    .recruitmentEndDate(
                            recruitsMembers ? recruitmentEndDate : null
                    )
                    .parentActivity(request.getParentActivity())
                    .listed(!Boolean.TRUE.equals(request.getPersonalProject()))
                    .participantLimit(request.getParticipantLimit())
                    .depositAmount(usesDeposit ? depositAmount : 0)
                    .recruitmentPositions(request.getRecruitmentPositions())
                    .operationPlan(request.getOperationPlan())
                    .instructorCareer(request.getInstructorCareer())
                    .build()
        );
        activity.synchronizeStatusFromSchedule(
                LocalDate.now(java.time.ZoneId.of("Asia/Seoul"))
        );

        Activity savedActivity = activityRepository.save(activity);
        lectureMaterialService.syncPrimaryMaterial(savedActivity, request.getMaterialUrl());

        Set<User> participants =
                new LinkedHashSet<>(request.getInitialMembers());

        if ("SPECIAL_LECTURE".equals(activityTypeCode)) {
            participants.removeIf(user -> user.getId().equals(request.getApplicant().getId()));
        } else {
            participants.add(request.getApplicant());
        }

        participants.forEach(user -> {
            ActivityParticipant participant = ActivityParticipant.create(
                    ActivityParticipantCreateCommand.builder()
                            .activity(savedActivity)
                            .user(user)
                            .status(ActivityParticipantStatus.APPROVED)
                            .build()
            );

            participantRepository.save(participant);
        });

        request.approve(
                findUser(reviewerId),
                normalizeComment(comment),
                savedActivity
        );

        return toResponse(request);
    }

    private RequestReferences resolveReferences(UUID applicantId, ActivityOpeningRequestDto dto) {
        ActivityType activityType = activityTypeRepository.findById(dto.getActivityTypeId())
                .orElseThrow(() -> new NotFoundException("활동 유형을 찾을 수 없습니다."));
        if (!Set.of("PROJECT", "STUDY", "SPECIAL_LECTURE").contains(activityType.getCode())) {
            throw new BadRequestException("강의, 프로젝트, 스터디만 개설을 신청할 수 있습니다.");
        }
        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));
        Activity parentActivity = dto.getParentActivityId() == null ? null :
                activityRepository.findById(dto.getParentActivityId())
                        .orElseThrow(() -> new NotFoundException("이전 활동을 찾을 수 없습니다."));
        if (parentActivity != null && !parentActivity.getAssignee().getId().equals(applicantId)) {
            throw new ForbiddenException("본인이 담당했던 활동만 이전 활동으로 선택할 수 있습니다.");
        }
        if (parentActivity != null && !parentActivity.getActivityType().getId().equals(activityType.getId())) {
            throw new BadRequestException("이전 활동과 같은 활동 유형을 선택해주세요.");
        }

        Set<User> initialMembers = new LinkedHashSet<>();
        if (dto.getInitialMemberIds() != null && !dto.getInitialMemberIds().isEmpty()) {
            List<User> users = userRepository.findAllById(dto.getInitialMemberIds());
            if (users.size() != dto.getInitialMemberIds().size()) {
                throw new BadRequestException("선택한 초기 참여자 중 확인할 수 없는 학회원이 있습니다.");
            }
            if (users.stream().anyMatch(user -> user.getMemberStatus() != MemberStatus.MEMBER)) {
                throw new BadRequestException("등록된 학회원만 초기 참여자로 선택할 수 있습니다.");
            }
            initialMembers.addAll(users);
        }
        return new RequestReferences(activityType, quarter, parentActivity, initialMembers);
    }

    private void validateRequest(UUID applicantId, ActivityOpeningRequestDto dto, RequestReferences references) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("종료일은 시작일 이후여야 합니다.");
        }

        boolean project = "PROJECT".equals(references.activityType().getCode());
        boolean specialLecture = "SPECIAL_LECTURE".equals(references.activityType().getCode());
        if (specialLecture && (dto.getInstructorCareer() == null || dto.getInstructorCareer().isBlank())) {
            throw new BadRequestException("강의자 경력을 입력해주세요.");
        }
        if (specialLecture && !references.initialMembers().isEmpty()) {
            throw new BadRequestException("강의 개설 신청에는 초기 참여자를 지정할 수 없습니다.");
        }
        if (Boolean.TRUE.equals(dto.getPersonalProject()) && !project) {
            throw new BadRequestException("개인 프로젝트는 프로젝트 유형에서만 선택할 수 있습니다.");
        }
        if (Boolean.TRUE.equals(dto.getPersonalProject()) &&
                (Boolean.TRUE.equals(dto.getAcceptsNewMembers()) || !references.initialMembers().isEmpty())) {
            throw new BadRequestException("개인 프로젝트에는 추가 참여자를 모집하거나 지정할 수 없습니다.");
        }
        if (dto.getStartDate().isBefore(references.quarter().getStartDate()) ||
                dto.getEndDate().isAfter(references.quarter().getEndDate())) {
            throw new BadRequestException("활동 기간은 선택한 분기 안에 있어야 합니다.");
        }

        long selectedMemberCount = references.initialMembers().stream()
                .filter(member -> !member.getId().equals(applicantId))
                .count();
        long registeredParticipantCount = selectedMemberCount + 1;
        if (dto.getExpectedMemberCount() < registeredParticipantCount) {
            throw new BadRequestException("예상 인원은 현재 선택한 참여 인원보다 적을 수 없습니다.");
        }
        long capacityParticipantCount = selectedMemberCount;
        if (dto.getParticipantLimit() != null
                && dto.getParticipantLimit() < capacityParticipantCount) {
            throw new BadRequestException("추가 참여 정원은 함께 시작할 학회원 수보다 적게 설정할 수 없습니다.");
        }
    }

    private void validateApprovalParticipantCounts(ActivityOpeningRequest request) {
        long selectedMemberCount = request.getInitialMembers().stream()
                .filter(member -> !member.getId().equals(request.getApplicant().getId()))
                .count();
        long registeredParticipantCount = selectedMemberCount + 1;
        if (request.getExpectedMemberCount() < registeredParticipantCount) {
            throw new BadRequestException("예상 인원은 실제 등록될 참여 인원보다 적을 수 없습니다.");
        }

        long capacityParticipantCount = selectedMemberCount;
        if (request.getParticipantLimit() != null
                && request.getParticipantLimit() < capacityParticipantCount) {
            throw new BadRequestException("추가 참여 정원은 실제 등록될 학회원 수보다 적을 수 없습니다.");
        }
    }

    /** 추가 모집을 하지 않으면 희망 포지션도 남기지 않는다. */
    /** 강의가 아닌 유형에는 강의자 경력을 남기지 않는다. */
    private String instructorCareerForRequest(ActivityOpeningRequestDto dto) {
        ActivityType activityType = activityTypeRepository
                .findById(dto.getActivityTypeId())
                .orElse(null);
        if (activityType == null || !"SPECIAL_LECTURE".equals(activityType.getCode())) {
            return null;
        }
        String career = dto.getInstructorCareer();
        if (career == null || career.isBlank()) return null;
        return career.trim();
    }

    private String materialUrlForRequest(ActivityOpeningRequestDto dto, ActivityType activityType) {
        String code = activityType.getCode();
        if (!"STUDY".equals(code) && !"SPECIAL_LECTURE".equals(code)) return null;
        return lectureMaterialService.normalizeOptionalMaterialUrl(dto.getMaterialUrl());
    }

    private String operationPlanForRequest(ActivityOpeningRequestDto dto, ActivityType activityType) {
        String operationPlan = dto.getOperationPlan();
        if (operationPlan == null || operationPlan.isBlank()) {
            if ("PROJECT".equals(activityType.getCode())) return "";
            throw new BadRequestException(
                    "STUDY".equals(activityType.getCode())
                            ? "스터디 계획서 링크를 첨부해주세요."
                            : "강의 계획서 링크를 첨부해주세요."
            );
        }
        return lectureMaterialService.normalizeOptionalMaterialUrl(operationPlan);
    }

    private String recruitmentPositionsForRequest(ActivityOpeningRequestDto dto) {
        if (!Boolean.TRUE.equals(dto.getAcceptsNewMembers())) return null;
        String positions = dto.getRecruitmentPositions();
        if (positions == null || positions.isBlank()) return null;
        return positions.trim();
    }

    private Integer participantLimitForRequest(ActivityOpeningRequestDto dto) {
        return Boolean.TRUE.equals(dto.getAcceptsNewMembers())
                ? dto.getParticipantLimit()
                : null;
    }

    private User findApplicant(UUID applicantId) {
        User applicant = findUser(applicantId);
        requireEligibleApplicant(applicant);
        return applicant;
    }

    private void validateSubmissionEligibility(ActivityOpeningRequest request) {
        requireEligibleApplicant(request.getApplicant());
        if (request.getInitialMembers().stream()
                .anyMatch(user -> user.getMemberStatus() != MemberStatus.MEMBER)) {
            throw new BadRequestException("현재 등록된 학회원만 초기 참여자로 지정할 수 있습니다.");
        }
    }

    private void requireEligibleApplicant(User applicant) {
        boolean privileged = applicant.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName())
                .anyMatch(role -> "ADMIN".equals(role) || "MANAGER".equals(role));
        if (applicant.getMemberStatus() != MemberStatus.MEMBER && !privileged) {
            throw new ForbiddenException("등록된 학회원만 개설을 신청할 수 있습니다.");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("학회원을 찾을 수 없습니다."));
    }

    private ActivityOpeningRequest find(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("활동 개설 신청을 찾을 수 없습니다."));
    }

    private ActivityOpeningRequest findForUpdate(UUID requestId) {
        return requestRepository.findByIdForUpdate(requestId)
                .orElseThrow(() -> new NotFoundException("활동 개설 신청을 찾을 수 없습니다."));
    }

    private ActivityOpeningRequest findOwned(UUID requestId, UUID applicantId) {
        ActivityOpeningRequest request = find(requestId);
        requireOwner(request, applicantId);
        return request;
    }

    private ActivityOpeningRequest findOwnedForUpdate(UUID requestId, UUID applicantId) {
        ActivityOpeningRequest request = findForUpdate(requestId);
        requireOwner(request, applicantId);
        return request;
    }

    private void requireOwner(ActivityOpeningRequest request, UUID applicantId) {
        if (!request.getApplicant().getId().equals(applicantId)) {
            throw new ForbiddenException("본인의 신청만 변경할 수 있습니다.");
        }
    }

    private void requireEditable(ActivityOpeningRequest request) {
        if (request.getStatus() != ActivityOpeningRequestStatus.DRAFT &&
                request.getStatus() != ActivityOpeningRequestStatus.REVISION_REQUESTED) {
            throw new BadRequestException("임시 저장 또는 보완 요청 상태의 신청만 수정할 수 있습니다.");
        }
    }

    private void requirePeriodOpen(ActivityOpeningRequest request, UUID quarterId) {
        if (request.getStatus() == ActivityOpeningRequestStatus.REVISION_REQUESTED) {
            openingPeriodService.requireRevisionOpen(quarterId);
            return;
        }
        openingPeriodService.requireApplicationOpen(quarterId);
    }

    private String normalizeComment(String comment) {
        return comment == null || comment.isBlank() ? null : comment.trim();
    }

    private ActivityOpeningRequestResponseDto toResponse(ActivityOpeningRequest request) {
        return ActivityOpeningRequestResponseDto.builder()
                .id(request.getId())
                .applicant(toUserSummary(request.getApplicant()))
                .title(request.getTitle())
                .description(request.getDescription())
                .operationPlan(request.getOperationPlan())
                .materialUrl(request.getMaterialUrl())
                .activityType(ActivityTypeResponseDto.builder()
                        .id(request.getActivityType().getId())
                        .name(request.getActivityType().getName())
                        .code(request.getActivityType().getCode())
                        .build())
                .quarter(QuarterResponseDto.builder()
                        .id(request.getQuarter().getId())
                        .name(request.getQuarter().getName())
                        .year(request.getQuarter().getYear())
                        .season(request.getQuarter().getSeason().name())
                        .startDate(request.getQuarter().getStartDate())
                        .endDate(request.getQuarter().getEndDate())
                        .build())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .expectedMemberCount(request.getExpectedMemberCount())
                .acceptsNewMembers(request.getAcceptsNewMembers())
                .participantLimit(request.getParticipantLimit())
                .recruitmentPositions(request.getRecruitmentPositions())
                .instructorCareer(request.getInstructorCareer())
                .personalProject(Boolean.TRUE.equals(request.getPersonalProject()))
                .parentActivityId(request.getParentActivity() == null ? null : request.getParentActivity().getId())
                .parentActivityTitle(request.getParentActivity() == null ? null : request.getParentActivity().getTitle())
                .initialMembers(request.getInitialMembers().stream()
                        .sorted(Comparator.comparing(User::getName))
                        .map(this::toUserSummary)
                        .toList())
                .status(request.getStatus().name())
                .reviewer(request.getReviewer() == null ? null : toUserSummary(request.getReviewer()))
                .reviewComment(request.getReviewComment())
                .submittedAt(request.getSubmittedAt())
                .reviewedAt(request.getReviewedAt())
                .approvedActivityId(request.getApprovedActivity() == null ? null : request.getApprovedActivity().getId())
                .createdAt(request.getCreatedAt())
                .modifiedAt(request.getModifiedAt())
                .build();
    }

    private UserSummaryResponseDto toUserSummary(User user) {
        return UserSummaryResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .studentId(user.getStudentId())
                .build();
    }

    private record RequestReferences(
            ActivityType activityType,
            Quarter quarter,
            Activity parentActivity,
            Set<User> initialMembers
    ) {}
}
