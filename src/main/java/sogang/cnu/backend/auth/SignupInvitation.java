package sogang.cnu.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.quarter.Quarter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signup_invitations")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "source_recruitment_id", unique = true)
    private UUID sourceRecruitmentId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "joined_quarter_id", nullable = false)
    private Quarter joinedQuarter;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant revokedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static SignupInvitation create(String name, Quarter joinedQuarter, Instant expiresAt) {
        return SignupInvitation.builder()
                .name(name)
                .joinedQuarter(joinedQuarter)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
    }

    public static SignupInvitation createForRecruitment(
            String name,
            UUID recruitmentId,
            Quarter joinedQuarter,
            Instant expiresAt
    ) {
        return SignupInvitation.builder()
                .name(name)
                .sourceRecruitmentId(recruitmentId)
                .joinedQuarter(joinedQuarter)
                .expiresAt(expiresAt)
                .createdAt(Instant.now())
                .build();
    }

    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void updateExpiration(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void revoke(Instant revokedAt) {
        this.revokedAt = revokedAt;
    }
}
