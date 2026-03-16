package sogang.cnu.backend.course_time_reservation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface CourseTimeReservationRepository
        extends JpaRepository<CourseTimeReservation, UUID>, CourseTimeReservationRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r FROM CourseTimeReservation r
            WHERE r.user.id = :userId
              AND r.id != :excludeId
              AND r.startAt < :newEnd
              AND r.endAt   > :newStart
            """)
    List<CourseTimeReservation> findOverlappingForUpdate(
            @Param("userId") UUID userId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd,
            @Param("excludeId") UUID excludeId
    );

    void deleteByActivityId(UUID activityId);
}
