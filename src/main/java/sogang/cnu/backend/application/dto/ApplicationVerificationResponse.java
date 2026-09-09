package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationVerificationResponse {
    private ApplicationResponse application;
    private String accessToken;
}
