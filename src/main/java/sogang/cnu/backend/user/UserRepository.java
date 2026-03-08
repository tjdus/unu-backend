package sogang.cnu.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, QuerydslPredicateExecutor<User> {
    Optional<User> findByStudentId(String studentId);
    Optional<User> findByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, UUID id);
    boolean existsByStudentIdAndIdNot(String studentId, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    boolean existsByGithubIdAndIdNot(String githubId, UUID id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);
}
