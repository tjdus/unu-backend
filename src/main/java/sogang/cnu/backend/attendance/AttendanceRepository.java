package sogang.cnu.backend.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findBySessionId(UUID sessionId);
    List<Attendance> findByParticipantId(UUID participantId);

    Long countByParticipantIdAndStatus(UUID participantId, AttendanceStatus status);

    Long countByParticipantIdAndStatusAndSessionDateLessThanEqual(
            UUID participantId,
            AttendanceStatus status,
            LocalDate date
    );

    Optional<Attendance> findBySessionIdAndParticipantId(UUID sessionId, UUID participantId);

    void deleteBySessionId(UUID sessionId);

    @Modifying
    @Query("DELETE FROM Attendance a WHERE a.participant.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") UUID activityId);
}
