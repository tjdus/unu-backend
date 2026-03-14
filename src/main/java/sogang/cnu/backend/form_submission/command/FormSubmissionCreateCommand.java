package sogang.cnu.backend.form_submission.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.user.User;

@Getter
@Builder
public class FormSubmissionCreateCommand {
    private Form form;
    private User user;
    private JsonNode answers;
    private JsonNode formSnapshot;
}
