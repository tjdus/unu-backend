package sogang.cnu.backend.activity_notice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;

import java.util.List;
import java.util.UUID;

public interface ActivityNoticeReadRepository extends JpaRepository<ActivityNoticeRead, UUID> {
    boolean existsByNoticeIdAndUserId(UUID noticeId, UUID userId);

    @Query("""
            SELECT receipt.notice.id
            FROM ActivityNoticeRead receipt
            WHERE receipt.user.id = :userId
              AND receipt.notice.activity.id = :activityId
            """)
    List<UUID> findReadNoticeIds(
            @Param("userId") UUID userId,
            @Param("activityId") UUID activityId
    );

    @Query("""
            SELECT notice.activity.id, COUNT(notice.id)
            FROM ActivityNotice notice
            WHERE EXISTS (
                  SELECT participant.id
                  FROM ActivityParticipant participant
                  WHERE participant.activity.id = notice.activity.id
                    AND participant.user.id = :userId
                    AND participant.status = :status
              )
              AND notice.activity.assignee.id <> :userId
              AND NOT EXISTS (
                  SELECT openingRequest.id
                  FROM ActivityOpeningRequest openingRequest
                  WHERE openingRequest.approvedActivity.id = notice.activity.id
                    AND openingRequest.applicant.id = :userId
              )
              AND NOT EXISTS (
                  SELECT receipt.id
                  FROM ActivityNoticeRead receipt
                  WHERE receipt.notice.id = notice.id
                    AND receipt.user.id = :userId
              )
            GROUP BY notice.activity.id
            """)
    List<Object[]> findUnreadCounts(
            @Param("userId") UUID userId,
            @Param("status") ActivityParticipantStatus status
    );

    @Modifying
    @Query("DELETE FROM ActivityNoticeRead receipt WHERE receipt.notice.id = :noticeId")
    void deleteByNoticeId(@Param("noticeId") UUID noticeId);

    @Modifying
    @Query("DELETE FROM ActivityNoticeRead receipt WHERE receipt.notice.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") UUID activityId);
}
