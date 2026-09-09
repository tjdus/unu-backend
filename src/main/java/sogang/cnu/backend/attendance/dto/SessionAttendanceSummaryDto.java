package sogang.cnu.backend.attendance.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

/** 일정 관리 탭의 세션별 출석 요약(회차마다 개별 조회 대신 활동 단위로 한 번에 집계). */
@Getter
@Builder
public class SessionAttendanceSummaryDto {
    private UUID sessionId;
    private long present;
    private long absent; // ABSENT + LATE
    private long excused;
    private long total;
}
