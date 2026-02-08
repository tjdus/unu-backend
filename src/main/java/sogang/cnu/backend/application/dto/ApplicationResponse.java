package sogang.cnu.backend.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationResponse {
    private Long id;
    private Long recruitmentId;
    private String name;
    private String studentId;
    private String major;
    private String subMajor;
    private String email;
    private String githubId;
    private String phoneNumber;
    private String status;
    private String submittedAt;
    private String reviewedAt;
    private JsonNode answers;
    private JsonNode formSnapshot;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}

