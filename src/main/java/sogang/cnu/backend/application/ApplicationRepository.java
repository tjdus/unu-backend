package sogang.cnu.backend.application;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import sogang.cnu.backend.recruitment.RecruitmentType;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    List<Application> findByRecruitmentId(UUID recruitmentId);
    Optional<Application> findFirstByNameAndEmailAndRecruitmentTypeOrderByCreatedAtDesc(
            String name, String email, RecruitmentType recruitmentType);
    @Query("""
            select a from Application a
            where a.recruitment.type = :type
              and (a.applicantUserId = :userId or (a.applicantUserId is null and a.studentId = :studentId))
              and a.status <> :excludedStatus
            order by a.createdAt desc
            """)
    List<Application> findMyOperationApplications(
            @Param("type") RecruitmentType type,
            @Param("userId") UUID userId,
            @Param("studentId") String studentId,
            @Param("excludedStatus") ApplicationStatus excludedStatus);
    /** 보유기간이 지난 신입 지원서: 불합격은 결정일, 그 외는 제출일 기준 (처리방침 제3조). */
    @Query("""
            select a from Application a
            where a.recruitment.type = :type
              and ((a.status = :rejected
                    and coalesce(a.reviewedAt, a.modifiedAt, a.submittedAt) < :rejectedCutoff)
                or (a.status <> :rejected and a.submittedAt < :submittedCutoff))
            """)
    List<Application> findExpiredForRetention(
            @Param("type") RecruitmentType type,
            @Param("rejected") ApplicationStatus rejected,
            @Param("rejectedCutoff") java.time.LocalDateTime rejectedCutoff,
            @Param("submittedCutoff") java.time.LocalDateTime submittedCutoff);
    boolean existsByRecruitmentIdAndStudentIdAndStatusNot(UUID recruitmentId, String studentId, ApplicationStatus status);
    boolean existsByRecruitmentIdAndEmailIgnoreCaseAndStatusNot(UUID recruitmentId, String email, ApplicationStatus status);
    boolean existsByRecruitmentIdAndStudentIdAndStatusNotAndIdNot(
            UUID recruitmentId, String studentId, ApplicationStatus status, UUID id);
    boolean existsByRecruitmentIdAndEmailIgnoreCaseAndStatusNotAndIdNot(
            UUID recruitmentId, String email, ApplicationStatus status, UUID id);
}
