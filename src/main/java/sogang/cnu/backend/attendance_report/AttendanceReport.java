package sogang.cnu.backend.attendance_report;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.attendance.Attendance;
import sogang.cnu.backend.attendance_report.command.AttendanceReportCreateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(
        name = "attendance_reports",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_report_attendance",
                        columnNames = {"attendance_id"}
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false, unique = true)
    private Attendance attendance;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static AttendanceReport create(AttendanceReportCreateCommand command) {
        return AttendanceReport.builder()
                .attendance(command.getAttendance())
                .title(command.getTitle())
                .content(command.getContent())
                .build();
    }
}
