package sogang.cnu.backend.attendance_report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceReportRepository extends JpaRepository<AttendanceReport, UUID> {
    Optional<AttendanceReport> findByAttendanceId(UUID attendanceId);
}
