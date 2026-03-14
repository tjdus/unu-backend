package sogang.cnu.backend.form.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class FormRequestDto {
    private UUID templateId;
    private String title;
    private String description;
    private JsonNode schema;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}

