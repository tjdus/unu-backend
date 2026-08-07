package sogang.cnu.backend.recruitment;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, UUID> {
    Optional<Recruitment> findFirstByActiveIsTrueOrderByCreatedAtDesc();

    // 진행 중이거나 아직 시작 전이면서 가장 먼저 마감되는 모집 1건.
    // active 플래그와 무관하게 startAt/endAt만으로 판단한다(홈 배너 전용 단일 진실 공급원).
    Optional<Recruitment> findFirstByEndAtAfterOrderByEndAtAsc(LocalDateTime now);
}

