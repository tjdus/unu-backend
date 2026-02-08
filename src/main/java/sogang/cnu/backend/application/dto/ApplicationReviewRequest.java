package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplicationReviewRequest {
    private String status;
}

