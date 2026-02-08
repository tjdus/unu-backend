package sogang.cnu.backend.form.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FormResponseDto {
    private Long id;
    private FormTemplateResponseDto template;
    private String title;
    private JsonNode schema;
    private String createdAt;
    private String modifiedAt;
    private String createdBy;
    private String modifiedBy;
}

