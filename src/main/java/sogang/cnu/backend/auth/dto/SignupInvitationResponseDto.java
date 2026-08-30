package sogang.cnu.backend.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SignupInvitationResponseDto {
    private UUID id;
    private String name;
    private UUID joinedQuarterId;
    private String joinedQuarterName;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;
    private long totalCount;
    private long usedCount;
    private String token;
    private List<SignupInvitationMemberResponseDto> members;
}
