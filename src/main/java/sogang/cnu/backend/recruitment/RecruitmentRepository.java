package sogang.cnu.backend.recruitment;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recruitment r where r.id = :id")
    Optional<Recruitment> findByIdForUpdate(@Param("id") UUID id);

    Optional<Recruitment> findFirstByTypeAndActiveIsTrueAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByEndAtAsc(
            RecruitmentType type, LocalDateTime startAt, LocalDateTime endAt);

    Optional<Recruitment> findFirstByTypeAndActiveIsTrueAndStartAtGreaterThanOrderByStartAtAsc(
            RecruitmentType type, LocalDateTime now);

    Optional<Recruitment> findFirstByTypeAndActiveIsTrueAndEndAtLessThanOrderByEndAtDesc(
            RecruitmentType type, LocalDateTime now);

    // 홈 CTA용: 공개(active=true) + 아직 끝나지 않은(endAt > now) 모집 중 가장 가까운 1건.
    Optional<Recruitment> findFirstByTypeAndActiveIsTrueAndEndAtAfterOrderByEndAtAsc(
            RecruitmentType type, LocalDateTime now);

    List<Recruitment> findAllByTypeOrderByStartAtDesc(RecruitmentType type);

    @Query("""
            SELECT r.id
            FROM Recruitment r
            WHERE r.type = :type
              AND r.createdAt >= :cutoff
              AND NOT EXISTS (
                SELECT receipt.id
                FROM RecruitmentCardRead receipt
                WHERE receipt.recruitment.id = r.id
                  AND receipt.user.id = :userId
              )
            """)
    List<UUID> findUnreadRecentIdsByType(
            @Param("type") RecruitmentType type,
            @Param("userId") UUID userId,
            @Param("cutoff") LocalDateTime cutoff
    );
}
