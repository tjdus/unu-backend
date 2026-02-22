package sogang.cnu.backend.form.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class FormTemplateResponseDto {
    private UUID id;
    private String title;
    private JsonNode schema;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}

