package sogang.cnu.backend.application.command;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.recruitment.Recruitment;

import java.util.UUID;

@Getter
@Builder
public class ApplicationCreateCommand {
    private Recruitment recruitment;
    private UUID applicantUserId;
    private String name;
    private String studentId;
    private String major;
    private String subMajor;
    private String email;
    private String githubId;
    private String phoneNumber;
    private JsonNode answers;
    private JsonNode formSnapshot;

    private String password;
}
