package sogang.cnu.backend.activity_notice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActivityNoticeRepository extends JpaRepository<ActivityNotice, UUID> {
    List<ActivityNotice> findAllByActivityIdOrderByCreatedAtDesc(UUID activityId);

    @Modifying
    @Query("DELETE FROM ActivityNotice notice WHERE notice.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") UUID activityId);
}
