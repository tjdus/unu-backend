package sogang.cnu.backend.form.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FormRequestDto {
    private Long templateId;
    private String title;
    private JsonNode schema;
}

