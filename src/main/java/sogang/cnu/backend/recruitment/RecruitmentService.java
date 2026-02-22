package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.form.FormRepository;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.recruitment.command.RecruitmentCreateCommand;
import sogang.cnu.backend.recruitment.command.RecruitmentUpdateCommand;
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
    public RecruitmentResponseDto update(UUID id, RecruitmentRequestDto dto) {
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));

        validateDates(dto.getStartAt(), dto.getEndAt());
        recruitment.update(toUpdateCommand(dto));
        return recruitmentMapper.toResponseDto(recruitment);
    }

    @Transactional
    public void delete(UUID id) {
        Recruitment recruitment = recruitmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Recruitment not found"));
        recruitmentRepository.delete(recruitment);
    }

    @Transactional
    public RecruitmentResponseDto getActiveRecruitment() {
        Recruitment recruitment = recruitmentRepository.findFirstByActiveIsTrueOrderByCreatedAtDesc()
                .orElseThrow(() -> new NotFoundException("Active recruitment not found"));
        return recruitmentMapper.toResponseDto(recruitment);
    }

    private void validateDates(java.time.LocalDateTime startAt, java.time.LocalDateTime endAt) {
        if (startAt != null && endAt != null && startAt.isAfter(endAt)) {
            throw new IllegalArgumentException("Start date must be before end date");
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
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .quarter(findQuarter(dto.getQuarterId()))
                .active(dto.getActive() != null ? dto.getActive() : true)
                .form(findForm(dto.getFormId()))
                .build();
    }

    private RecruitmentUpdateCommand toUpdateCommand(RecruitmentRequestDto dto) {
        return RecruitmentUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .quarter(findQuarter(dto.getQuarterId()))
                .active(dto.getActive())
                .form(findForm(dto.getFormId()))
                .build();
    }
}

