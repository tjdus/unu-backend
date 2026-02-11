package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttendanceStatsResponseDto {
    private Long presentCount;
    private Long absentCount;
    private Long excusedCount;
}
