package sogang.cnu.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") UUID id);

    Optional<User> findByStudentId(String studentId);
    Optional<User> findByUsername(String username);
    boolean existsByIdAndMemberStatusNot(UUID id, MemberStatus memberStatus);

    boolean existsByUsername(String username);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
    boolean existsByGithubId(String githubId);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findAllByStudentIdIn(Collection<String> studentIds);

    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByStudentIdAndIdNot(String studentId, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByGithubIdAndIdNot(String githubId, UUID id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);
}
