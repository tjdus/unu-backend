package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.form.FormRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.recruitment.command.RecruitmentCreateCommand;
import sogang.cnu.backend.recruitment.command.RecruitmentUpdateCommand;
import sogang.cnu.backend.recruitment.dto.RecruitmentCompletionMessageResponseDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruitmentService {
    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentMapper recruitmentMapper;
    private final FormRepository formRepository;
    private final QuarterRepository quarterRepository;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public RecruitmentResponseDto getById(UUID id) {
        log.debug("getById" + id);
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
        log.debug("getById" + recruitment);
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponseDto> getAll() {
        return recruitmentRepository.findAll().stream()
                .map(recruitmentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RecruitmentResponseDto create(RecruitmentRequestDto dto) {
        validateDates(dto.getStartAt(), dto.getEndAt());
        RecruitmentCreateCommand createCommand = toCreateCommand(dto);
        Recruitment recruitment = Recruitment.create(createCommand);
        Recruitment savedRecruitment = recruitmentRepository.save(recruitment);
        return recruitmentMapper.toResponseDto(savedRecruitment);
    }

    @Transactional
    public RecruitmentResponseDto update(UUID userId, UUID id, RecruitmentRequestDto dto) {
        permissionChecker.checkManagerOrAdmin(userId);
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));

        validateDates(dto.getStartAt(), dto.getEndAt());
        RecruitmentType newType = resolveType(dto.getType());
        if (recruitment.getType() == RecruitmentType.INTERNAL_OPERATION
                && newType == RecruitmentType.NEW_MEMBER
                && !recruitment.getApplications().isEmpty()) {
            throw new BadRequestException("신청 내역이 있는 운영 모집은 신규 학회원 모집으로 변경할 수 없습니다.");
        }
        recruitment.update(toUpdateCommand(dto));
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        permissionChecker.checkManagerOrAdmin(userId);
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
        recruitmentRepository.delete(recruitment);
    }

    @Transactional
    public RecruitmentResponseDto getActiveRecruitment() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        Recruitment recruitment = recruitmentRepository
                .findFirstByTypeAndActiveIsTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByEndAtAsc(
                        RecruitmentType.NEW_MEMBER, now, now)
                .or(() -> recruitmentRepository.findFirstByTypeAndActiveIsTrueAndStartAtGreaterThanOrderByStartAtAsc(
                        RecruitmentType.NEW_MEMBER, now))
                .or(() -> recruitmentRepository.findFirstByTypeAndActiveIsTrueAndEndAtLessThanOrderByEndAtDesc(
                        RecruitmentType.NEW_MEMBER, now))
                .orElseThrow(() -> new NotFoundException("Active recruitment not found"));
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional(readOnly = true)
    public RecruitmentResponseDto getClosestRecruitment() {
        Recruitment recruitment = recruitmentRepository
                .findFirstByTypeAndEndAtAfterOrderByEndAtAsc(
                        RecruitmentType.NEW_MEMBER, java.time.LocalDateTime.now())
                .orElseThrow(() -> new NotFoundException("No upcoming or ongoing recruitment"));
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional(readOnly = true)
    public List<RecruitmentResponseDto> getOperationRecruitments() {
        return recruitmentRepository.findAllByTypeOrderByStartAtDesc(RecruitmentType.INTERNAL_OPERATION).stream()
                .map(recruitmentMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecruitmentResponseDto getOperationRecruitment(UUID id) {
        Recruitment recruitment = findOperationRecruitment(id);
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional(readOnly = true)
    public RecruitmentCompletionMessageResponseDto getOperationCompletionMessage(UUID id) {
        Recruitment recruitment = findOperationRecruitment(id);
        return RecruitmentCompletionMessageResponseDto.builder()
                .completionMessage(recruitment.getCompletionMessage())
                .build();
    }

    @Transactional(readOnly = true)
    public RecruitmentCompletionMessageResponseDto getCompletionMessage(UUID id) {
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
        if (recruitment.getType() != RecruitmentType.NEW_MEMBER) {
            throw new NotFoundException("Recruitment not found");
        }
        return RecruitmentCompletionMessageResponseDto.builder()
                .completionMessage(recruitment.getCompletionMessage())
                .build();
    }

    private void validateDates(java.time.LocalDateTime startAt, java.time.LocalDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new BadRequestException("모집 종료 시각은 시작 시각보다 이후여야 합니다.");
        }
    }

    private Form findForm(UUID formId) {
        return formRepository.findById(formId)
                .orElseThrow(() -> new NotFoundException("Form not found"));
    }

    private Quarter findQuarter(UUID quarterId) {
        if (quarterId == null) {
            return null;
        }
        return quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));
    }

    private RecruitmentCreateCommand toCreateCommand(RecruitmentRequestDto dto) {
        return RecruitmentCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .completionMessage(normalizeOptionalText(dto.getCompletionMessage()))
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .quarter(findQuarter(dto.getQuarterId()))
                .active(dto.getActive() != null ? dto.getActive() : true)
                .form(findForm(dto.getFormId()))
                .type(resolveType(dto.getType()))
                .build();
    }

    private RecruitmentUpdateCommand toUpdateCommand(RecruitmentRequestDto dto) {
        return RecruitmentUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .completionMessage(normalizeOptionalText(dto.getCompletionMessage()))
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .quarter(findQuarter(dto.getQuarterId()))
                .active(dto.getActive())
                .form(findForm(dto.getFormId()))
                .type(resolveType(dto.getType()))
                .build();
    }

    private RecruitmentType resolveType(RecruitmentType type) {
        return type == null ? RecruitmentType.NEW_MEMBER : type;
    }

    private Recruitment findOperationRecruitment(UUID id) {
        return recruitmentRepository.findById(id)
                .filter(item -> item.getType() == RecruitmentType.INTERNAL_OPERATION)
                .orElseThrow(() -> new NotFoundException("Operation recruitment not found"));
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.length() > 1000) {
            throw new BadRequestException("완료 안내는 1000자 이하로 입력해야 합니다.");
        }
        return normalized;
    }
}
