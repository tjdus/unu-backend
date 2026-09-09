package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SignupEligibilityResponseDto {
    private String invitationName;
    private String studentId;
    private UUID joinedQuarterId;
    private String joinedQuarterName;
    private String name;
    private String major;
    private String subMajor;
    private String email;
    private String githubId;
    private String phoneNumber;
}
