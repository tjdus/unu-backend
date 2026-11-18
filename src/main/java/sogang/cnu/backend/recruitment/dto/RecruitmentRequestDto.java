package sogang.cnu.backend.recruitment.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.recruitment.RecruitmentType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RecruitmentRequestDto {
    private String title;
    private String description;
    private String completionMessage;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private UUID quarterId;
    private Boolean active;
    /** The application form's questions ({ version, questions: [...] }). */
    private JsonNode questions;
    private RecruitmentType type;
}
