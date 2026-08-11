package sogang.cnu.backend.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sogang.cnu.backend.common.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationAnswerValidatorTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ApplicationAnswerValidator validator;
    private JsonNode schema;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ApplicationAnswerValidator(objectMapper);
        schema = objectMapper.readTree("""
                {
                  "version": 1,
                  "questions": [
                    {"id":"intro","type":"LONG_TEXT","title":"소개","required":true},
                    {"id":"track","type":"SINGLE_CHOICE","title":"분야","required":true,"options":["FE","BE"]},
                    {"id":"interests","type":"MULTIPLE_CHOICE","title":"관심사","required":false,"options":["WEB","AI"]}
                  ]
                }
                """);
    }

    @Test
    void acceptsAnswersMatchingSchema() throws Exception {
        JsonNode answers = objectMapper.readTree("""
                {"intro":"안녕하세요","track":"FE","interests":["WEB"]}
                """);

        validator.validate(schema, answers);
    }

    @Test
    void rejectsMissingRequiredAnswer() throws Exception {
        JsonNode answers = objectMapper.readTree("{\"track\":\"FE\"}");

        assertThatThrownBy(() -> validator.validate(schema, answers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("필수 질문에 모두 답변해주세요.");
    }

    @Test
    void rejectsChoiceOutsideConfiguredOptions() throws Exception {
        JsonNode answers = objectMapper.readTree("{\"intro\":\"안녕하세요\",\"track\":\"DESIGN\"}");

        assertThatThrownBy(() -> validator.validate(schema, answers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("선택형 답변이 보기와 일치하지 않습니다.");
    }

    @Test
    void rejectsUnknownQuestion() throws Exception {
        JsonNode answers = objectMapper.readTree(
                "{\"intro\":\"안녕하세요\",\"track\":\"FE\",\"unknown\":\"value\"}");

        assertThatThrownBy(() -> validator.validate(schema, answers))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("지원서에 존재하지 않는 질문의 답변이 포함되어 있습니다.");
    }
}
