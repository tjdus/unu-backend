package sogang.cnu.backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sogang.cnu.backend.common.exception.BadRequestException;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ApplicationAnswerValidator {
    private static final int MAX_TEXT_LENGTH = 20_000;

    private final ObjectMapper objectMapper;

    public void validate(JsonNode rawSchema, JsonNode answers) {
        JsonNode schema = normalizeSchema(rawSchema);
        JsonNode questions = schema.get("questions");

        if (questions == null || !questions.isArray() || answers == null || !answers.isObject()) {
            throw new BadRequestException("지원서 양식 또는 답변 형식이 올바르지 않습니다.");
        }

        Set<String> questionIds = new HashSet<>();
        for (JsonNode question : questions) {
            String id = textValue(question, "id");
            String type = textValue(question, "type");
            boolean required = question.path("required").asBoolean(false);
            if (id == null || type == null || !questionIds.add(id)) {
                throw new BadRequestException("지원서 질문 구성이 올바르지 않습니다.");
            }

            JsonNode answer = answers.get(id);
            if (isEmpty(answer)) {
                if (required) {
                    throw new BadRequestException("필수 질문에 모두 답변해주세요.");
                }
                continue;
            }

            validateAnswer(type, question.path("options"), answer);
        }

        answers.fieldNames().forEachRemaining(id -> {
            if (!questionIds.contains(id)) {
                throw new BadRequestException("지원서에 존재하지 않는 질문의 답변이 포함되어 있습니다.");
            }
        });
    }

    private static final Set<String> QUESTION_TYPES =
            Set.of("SHORT_TEXT", "LONG_TEXT", "SINGLE_CHOICE", "MULTIPLE_CHOICE");

    /**
     * Checks a recruitment's questions ({ version, questions: [...] }) and returns them as an
     * object, so they are always stored in one shape (a JSON string is parsed first).
     */
    public JsonNode normalizeQuestions(JsonNode rawSchema) {
        JsonNode schema = normalizeSchema(rawSchema);
        JsonNode questions = schema.get("questions");
        if (!schema.isObject() || questions == null || !questions.isArray()) {
            throw new BadRequestException("지원서 질문 형식이 올바르지 않습니다.");
        }

        Set<String> questionIds = new HashSet<>();
        for (JsonNode question : questions) {
            String id = textValue(question, "id");
            String type = textValue(question, "type");
            String title = textValue(question, "title");
            if (id == null || !questionIds.add(id) || type == null || !QUESTION_TYPES.contains(type)
                    || title == null || title.isBlank()) {
                throw new BadRequestException("지원서 질문 구성이 올바르지 않습니다.");
            }
            boolean choice = type.equals("SINGLE_CHOICE") || type.equals("MULTIPLE_CHOICE");
            if (choice && (!question.path("options").isArray() || question.path("options").isEmpty())) {
                throw new BadRequestException("선택형 질문에는 보기가 1개 이상 있어야 합니다.");
            }
        }
        return schema;
    }

    private JsonNode normalizeSchema(JsonNode schema) {
        if (schema == null) {
            throw new BadRequestException("지원서 양식이 없습니다.");
        }
        if (!schema.isTextual()) {
            return schema;
        }
        try {
            return objectMapper.readTree(schema.asText());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("지원서 양식이 올바르지 않습니다.");
        }
    }

    private void validateAnswer(String type, JsonNode options, JsonNode answer) {
        switch (type) {
            case "SHORT_TEXT", "LONG_TEXT" -> {
                if (!answer.isTextual() || answer.asText().length() > MAX_TEXT_LENGTH) {
                    throw new BadRequestException("텍스트 답변 형식이 올바르지 않습니다.");
                }
            }
            case "SINGLE_CHOICE" -> {
                if (!answer.isTextual() || !containsOption(options, answer.asText())) {
                    throw new BadRequestException("선택형 답변이 보기와 일치하지 않습니다.");
                }
            }
            case "MULTIPLE_CHOICE" -> {
                if (!answer.isArray()) {
                    throw new BadRequestException("복수 선택 답변 형식이 올바르지 않습니다.");
                }
                for (JsonNode selected : answer) {
                    if (!selected.isTextual() || !containsOption(options, selected.asText())) {
                        throw new BadRequestException("복수 선택 답변이 보기와 일치하지 않습니다.");
                    }
                }
            }
            default -> throw new BadRequestException("지원하지 않는 질문 유형입니다.");
        }
    }

    private boolean containsOption(JsonNode options, String selected) {
        if (!options.isArray()) {
            return false;
        }
        for (JsonNode option : options) {
            if (option.isTextual() && option.asText().equals(selected)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEmpty(JsonNode answer) {
        return answer == null || answer.isNull()
                || (answer.isTextual() && answer.asText().isBlank())
                || (answer.isArray() && answer.isEmpty());
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value != null && value.isTextual() && !value.asText().isBlank()
                ? value.asText()
                : null;
    }
}
