package sogang.cnu.backend.auth;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface SignupInvitationRepository extends JpaRepository<SignupInvitation, UUID> {
    List<SignupInvitation> findAllByOrderByCreatedAtDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT invitation FROM SignupInvitation invitation WHERE invitation.sourceRecruitmentId = :recruitmentId")
    Optional<SignupInvitation> findBySourceRecruitmentIdForUpdate(@Param("recruitmentId") UUID recruitmentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT invitation FROM SignupInvitation invitation WHERE invitation.id = :id")
    Optional<SignupInvitation> findByIdForUpdate(@Param("id") UUID id);
}
