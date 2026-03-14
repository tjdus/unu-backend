package sogang.cnu.backend.form_submission.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class FormSubmissionRequestDto {
    private UUID formId;
    private JsonNode answers;
}
