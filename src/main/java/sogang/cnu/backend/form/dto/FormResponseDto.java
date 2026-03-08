package sogang.cnu.backend.form.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.common.domain.dto.AuditorDto;

import java.util.UUID;

@Getter
@Setter
@Builder
public class FormResponseDto {
    private UUID id;
    private FormTemplateResponseDto template;
    private String title;
    private JsonNode schema;
    private String createdAt;
    private String modifiedAt;
    private AuditorDto createdBy;
    private AuditorDto modifiedBy;
}

