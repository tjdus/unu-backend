package sogang.cnu.backend.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID>, ActivityRepositoryCustom {

    @Modifying
    @Query("UPDATE Activity a SET a.parentActivity = null WHERE a.parentActivity.id = :activityId")
    void detachChildActivities(@Param("activityId") UUID activityId);
}
