package sogang.cnu.backend.activity_opening_request;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivityOpeningRequestRepository extends JpaRepository<ActivityOpeningRequest, UUID> {
    List<ActivityOpeningRequest> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId);

    List<ActivityOpeningRequest> findAllByOrderByCreatedAtDesc();

    List<ActivityOpeningRequest> findAllByApprovedActivityIsNotNull();

    Optional<ActivityOpeningRequest> findByApprovedActivityId(UUID activityId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ActivityOpeningRequest r where r.id = :id")
    Optional<ActivityOpeningRequest> findByIdForUpdate(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE ActivityOpeningRequest r SET r.parentActivity = null WHERE r.parentActivity.id = :activityId")
    void detachParentActivity(@Param("activityId") UUID activityId);
}
