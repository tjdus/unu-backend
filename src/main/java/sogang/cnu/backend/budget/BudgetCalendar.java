package sogang.cnu.backend.budget;

import sogang.cnu.backend.quarter.Quarter;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 예산 계획의 (분기, 월)을 실제 달력 연월로 바꾸는 기준.
 * 겨울학기는 분기의 year와 달력 연도가 다르다(예: 2025 WINTER = 2026-01-01 ~ 2026-02-28).
 * 내보내기·이월금·업로드가 모두 이 기준을 써야 연도가 서로 어긋나지 않는다.
 */
public final class BudgetCalendar {

    private BudgetCalendar() {}

    /** 분기 기간 안에 그 월이 있으면 그 달력 연도, 없으면(기간 밖 월) quarter.year로 본다. */
    public static YearMonth toYearMonth(Quarter quarter, int month) {
        LocalDate start = quarter.getStartDate();
        LocalDate end = quarter.getEndDate();
        if (start != null && end != null) {
            for (int year : new int[]{start.getYear(), end.getYear()}) {
                YearMonth candidate = YearMonth.of(year, month);
                if (overlaps(candidate, start, end)) {
                    return candidate;
                }
            }
        }
        return YearMonth.of(quarter.getYear(), month);
    }

    /** 이 분기의 기간이 해당 달력 연월을 포함하는지 */
    public static boolean covers(Quarter quarter, YearMonth yearMonth) {
        LocalDate start = quarter.getStartDate();
        LocalDate end = quarter.getEndDate();
        return start != null && end != null && overlaps(yearMonth, start, end);
    }

    private static boolean overlaps(YearMonth yearMonth, LocalDate start, LocalDate end) {
        return !yearMonth.atEndOfMonth().isBefore(start) && !yearMonth.atDay(1).isAfter(end);
    }
}
