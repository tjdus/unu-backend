package sogang.cnu.backend.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationCreateRequest {
    private Long recruitmentId;
    private String name;
    private String studentId;
    private String major;
    private String subMajor;
    private String email;
    private String githubId;
    private String phoneNumber;
    private JsonNode answers;
}
