package sogang.cnu.backend.form_submission.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.util.UUID;

@Getter
@Setter
@Builder
public class FormSubmissionResponseDto {
    private UUID id;
    private UUID formId;
    private UUID userId;
    private JsonNode answers;
    private JsonNode formSnapshot;
    private String submittedAt;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
}
