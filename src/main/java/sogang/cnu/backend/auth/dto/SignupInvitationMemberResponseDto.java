package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class SignupInvitationMemberResponseDto {
    private UUID id;
    private String studentId;
    private UUID userId;
    private String userName;
    private Instant usedAt;
}
