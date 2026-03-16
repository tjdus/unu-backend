package sogang.cnu.backend.form_submission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.form.FormRepository;
import sogang.cnu.backend.form_submission.command.FormSubmissionCreateCommand;
import sogang.cnu.backend.form_submission.dto.FormSubmissionRequestDto;
import sogang.cnu.backend.form_submission.dto.FormSubmissionResponseDto;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;
    private final FormSubmissionMapper formSubmissionMapper;
    private final FormRepository formRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public FormSubmissionResponseDto getById(UUID id) {
        FormSubmission submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormSubmission not found"));
        return formSubmissionMapper.toResponseDto(submission);
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionResponseDto> getByFormId(UUID formId) {
        return formSubmissionRepository.findByFormId(formId).stream()
                .map(formSubmissionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FormSubmissionResponseDto> getByUserId(UUID userId) {
        return formSubmissionRepository.findByUserId(userId).stream()
                .map(formSubmissionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FormSubmissionResponseDto create(UUID userId, FormSubmissionRequestDto dto) {
        Form form = formRepository.findById(dto.getFormId())
                .orElseThrow(() -> new NotFoundException("Form not found"));

        validateFormPeriod(form);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        FormSubmissionCreateCommand command = FormSubmissionCreateCommand.builder()
                .form(form)
                .user(user)
                .answers(dto.getAnswers())
                .formSnapshot(form.getSchema())
                .build();

        FormSubmission submission = FormSubmission.create(command);
        FormSubmission saved = formSubmissionRepository.save(submission);
        return formSubmissionMapper.toResponseDto(saved);
    }

    private void validateFormPeriod(Form form) {
        LocalDateTime startAt = form.getStartAt();
        LocalDateTime endAt = form.getEndAt();

        if (startAt == null && endAt == null) {
            return;
        }

        LocalDateTime nowKst = ZonedDateTime.now(ZoneId.systemDefault()).toLocalDateTime();

        if (startAt != null && nowKst.isBefore(startAt)) {
            throw new BadRequestException("아직 제출 가능한 기간이 아닙니다.");
        }
        if (endAt != null && nowKst.isAfter(endAt)) {
            throw new BadRequestException("제출 가능한 기간이 종료되었습니다.");
        }
    }

    @Transactional
    public void delete(UUID id) {
        FormSubmission submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormSubmission not found"));
        formSubmissionRepository.delete(submission);
    }
}
