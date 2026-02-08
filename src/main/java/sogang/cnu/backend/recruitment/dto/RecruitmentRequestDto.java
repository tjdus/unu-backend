package sogang.cnu.backend.recruitment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class RecruitmentRequestDto {
    private String title;
    private String description;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Long quarterId;
    private Boolean active;
    private Long formId;
}

