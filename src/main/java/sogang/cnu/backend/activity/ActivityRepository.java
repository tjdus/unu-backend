package sogang.cnu.backend.activity;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID>, ActivityRepositoryCustom {

    List<Activity> findByAssigneeId(UUID assigneeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Activity a WHERE a.id = :id")
    Optional<Activity> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Activity a SET a.parentActivity = null WHERE a.parentActivity.id = :activityId")
    void detachChildActivities(@Param("activityId") UUID activityId);
}
