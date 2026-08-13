package sogang.cnu.backend.form_submission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.form.FormRepository;
import sogang.cnu.backend.form_submission.command.FormSubmissionCreateCommand;
import sogang.cnu.backend.form_submission.dto.FormSubmissionRequestDto;
import sogang.cnu.backend.form_submission.dto.FormSubmissionResponseDto;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormSubmissionService {

    private final FormSubmissionRepository formSubmissionRepository;
    private final FormSubmissionMapper formSubmissionMapper;
    private final FormRepository formRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public FormSubmissionResponseDto getById(UUID id) {
        FormSubmission submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormSubmission not found"));
        requireOwnerOrManager(submission);
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
        validateAnswers(form.getSchema(), dto.getAnswers());

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

    private void validateAnswers(JsonNode schema, JsonNode answers) {
        JsonNode parsedSchema = parseSchema(schema);
        if (parsedSchema == null || !parsedSchema.isObject()
                || parsedSchema.get("questions") == null
                || !parsedSchema.get("questions").isArray()) {
            throw new BadRequestException("신청서 문항 설정이 올바르지 않습니다.");
        }
        if (answers == null || !answers.isObject()) {
            throw new BadRequestException("신청서 답변 형식이 올바르지 않습니다.");
        }

        Set<String> questionIds = new HashSet<>();
        for (JsonNode question : parsedSchema.get("questions")) {
            String questionId = requiredText(question, "id");
            String questionType = requiredText(question, "type");
            if (!questionIds.add(questionId)) {
                throw new BadRequestException("신청서에 중복된 문항 ID가 있습니다.");
            }

            JsonNode answer = answers.get(questionId);
            boolean required = question.path("required").asBoolean(false);
            if (required && isEmptyAnswer(answer)) {
                throw new BadRequestException("필수 문항에 답변해주세요: " + question.path("title").asText(questionId));
            }
            if (answer == null || answer.isNull()) {
                continue;
            }

            validateAnswer(questionType, question, answer);
        }

        answers.fieldNames().forEachRemaining(answerId -> {
            if (!questionIds.contains(answerId)) {
                throw new BadRequestException("존재하지 않는 문항의 답변이 포함되어 있습니다.");
            }
        });
    }

    private JsonNode parseSchema(JsonNode schema) {
        if (schema == null || !schema.isTextual()) {
            return schema;
        }
        try {
            return objectMapper.readTree(schema.asText());
        } catch (Exception exception) {
            throw new BadRequestException("신청서 문항 설정이 올바르지 않습니다.");
        }
    }

    private void validateAnswer(String type, JsonNode question, JsonNode answer) {
        switch (type) {
            case "SHORT_TEXT", "LONG_TEXT" -> {
                if (!answer.isTextual()) {
                    throw new BadRequestException("텍스트 문항의 답변 형식이 올바르지 않습니다.");
                }
            }
            case "SINGLE_CHOICE" -> {
                if (!answer.isTextual() || !allowedOptions(question).contains(answer.asText())) {
                    throw new BadRequestException("선택지에 없는 답변이 포함되어 있습니다.");
                }
            }
            case "MULTIPLE_CHOICE" -> {
                if (!answer.isArray()) {
                    throw new BadRequestException("다중 선택 문항의 답변 형식이 올바르지 않습니다.");
                }
                Set<String> allowed = allowedOptions(question);
                Set<String> selected = new HashSet<>();
                for (JsonNode value : answer) {
                    if (!value.isTextual()
                            || !allowed.contains(value.asText())
                            || !selected.add(value.asText())) {
                        throw new BadRequestException("다중 선택 답변에 올바르지 않은 값이 포함되어 있습니다.");
                    }
                }
            }
            default -> throw new BadRequestException("지원하지 않는 문항 유형입니다: " + type);
        }
    }

    private Set<String> allowedOptions(JsonNode question) {
        JsonNode options = question.get("options");
        if (options == null || !options.isArray()) {
            throw new BadRequestException("선택형 문항의 선택지 설정이 올바르지 않습니다.");
        }
        Set<String> values = new HashSet<>();
        for (JsonNode option : options) {
            if (!option.isTextual() || option.asText().isBlank() || !values.add(option.asText())) {
                throw new BadRequestException("선택형 문항의 선택지 설정이 올바르지 않습니다.");
            }
        }
        return values;
    }

    private String requiredText(JsonNode node, String field) {
        if (node == null || !node.isObject()
                || node.get(field) == null
                || !node.get(field).isTextual()
                || node.get(field).asText().isBlank()) {
            throw new BadRequestException("신청서 문항 설정이 올바르지 않습니다.");
        }
        return node.get(field).asText();
    }

    private boolean isEmptyAnswer(JsonNode answer) {
        return answer == null
                || answer.isNull()
                || (answer.isTextual() && answer.asText().isBlank())
                || (answer.isArray() && answer.isEmpty());
    }

    @Transactional
    public void delete(UUID id) {
        FormSubmission submission = formSubmissionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormSubmission not found"));
        requireOwnerOrManager(submission);
        formSubmissionRepository.delete(submission);
    }

    // 본인이 제출한 것이거나 MANAGER/ADMIN이어야 조회·삭제할 수 있다.
    private void requireOwnerOrManager(FormSubmission submission) {
        boolean isOwner = submission.getUser() != null
                && submission.getUser().getId().equals(SecurityUtils.getCurrentUserId());
        if (!isOwner && !SecurityUtils.isManagerOrAdmin()) {
            throw new ForbiddenException("본인이 제출한 신청서만 확인할 수 있습니다.");
        }
    }
}
