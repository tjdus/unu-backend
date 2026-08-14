package sogang.cnu.backend.notice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface NoticeReadRepository extends JpaRepository<NoticeRead, UUID> {
    boolean existsByNoticeIdAndUserId(UUID noticeId, UUID userId);

    @Query("""
            SELECT notice.id
            FROM Notice notice
            WHERE notice.notificationEnabled = true
              AND NOT EXISTS (
                SELECT receipt.id
                FROM NoticeRead receipt
                WHERE receipt.notice.id = notice.id
                  AND receipt.user.id = :userId
            )
            ORDER BY notice.createdAt DESC
            """)
    List<UUID> findUnreadNoticeIds(@Param("userId") UUID userId);

    @Modifying
    @Query("DELETE FROM NoticeRead receipt WHERE receipt.notice.id = :noticeId")
    void deleteByNoticeId(@Param("noticeId") UUID noticeId);
}
