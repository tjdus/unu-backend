package sogang.cnu.backend.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SignupInvitationMemberRepository extends JpaRepository<SignupInvitationMember, UUID> {
    List<SignupInvitationMember> findByInvitationIdOrderByStudentIdAsc(UUID invitationId);

    long countByInvitationId(UUID invitationId);

    long countByInvitationIdAndUsedAtIsNotNull(UUID invitationId);

    boolean existsByInvitationIdAndStudentId(UUID invitationId, String studentId);

    Optional<SignupInvitationMember> findByInvitationIdAndStudentId(UUID invitationId, String studentId);

    /** 지원서에서 복사한 합격자 정보 중 보유기간(6개월)이 지난 것. */
    @Query("""
            SELECT member
            FROM SignupInvitationMember member
            WHERE member.invitation.sourceRecruitmentId IS NOT NULL
              AND member.invitation.createdAt < :cutoff
              AND (member.name IS NOT NULL OR member.email IS NOT NULL OR member.phoneNumber IS NOT NULL)
            """)
    List<SignupInvitationMember> findCopiedProfilesCreatedBefore(@Param("cutoff") java.time.Instant cutoff);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT member
            FROM SignupInvitationMember member
            WHERE member.invitation.id = :invitationId
              AND member.studentId = :studentId
            """)
    Optional<SignupInvitationMember> findForSignup(
            @Param("invitationId") UUID invitationId,
            @Param("studentId") String studentId
    );
}
