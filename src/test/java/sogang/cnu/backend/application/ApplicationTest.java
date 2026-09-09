package sogang.cnu.backend.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sogang.cnu.backend.application.command.ApplicationUpdateCommand;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void updatePersistsChangedAnswers() throws Exception {
        Application application = Application.builder()
                .answers(objectMapper.readTree("{\"question\":\"before\"}"))
                .build();
        ApplicationUpdateCommand command = ApplicationUpdateCommand.builder()
                .answers(objectMapper.readTree("{\"question\":\"after\"}"))
                .build();

        application.update(command);

        assertThat(application.getAnswers().get("question").asText()).isEqualTo("after");
    }
}
