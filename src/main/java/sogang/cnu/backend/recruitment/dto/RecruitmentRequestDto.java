package sogang.cnu.backend.recruitment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class RecruitmentRequestDto {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private UUID quarterId;
    private Boolean active;
    private UUID formId;
}

