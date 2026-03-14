package sogang.cnu.backend.attendance_report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceReportRepository extends JpaRepository<AttendanceReport, UUID> {
    Optional<AttendanceReport> findByAttendanceId(UUID attendanceId);

    @Modifying
    @Query("DELETE FROM AttendanceReport ar WHERE ar.attendance.participant.activity.id = :activityId")
    void deleteByActivityId(@Param("activityId") UUID activityId);
}
