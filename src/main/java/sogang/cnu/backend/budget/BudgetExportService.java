package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.quarter.Quarter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 가계부 데이터를 총무의 기존 구글 시트 양식(.xlsx)으로 내보낸다.
 * 시트 구성: '예산안'(카테고리 행 × 1~12월 열, 예상 블록 + 실제 블록), '수입지출 총액'(스터디 보증금 건별 내역).
 */
@Service
@RequiredArgsConstructor
public class BudgetExportService {

    private static final int MONTH_COUNT = 12;
    private static final int COL_LABEL = 0;
    private static final int COL_FIRST_MONTH = 1;   // B열 = 1월
    private static final int COL_TOTAL = 13;        // N열 = 합계
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. M. d");

    private final BudgetPlanRepository budgetPlanRepository;
    private final StudyDepositLedgerEntryRepository ledgerRepository;

    @Transactional(readOnly = true)
    public byte[] exportYear(int year) {
        // 겨울학기(quarter.year = year-1)에 속한 1~2월까지 포함하려고 두 해의 분기를 후보로 가져온 뒤 달력 연도로 거른다
        List<Integer> candidateYears = List.of(year - 1, year);
        List<BudgetPlan> plans = budgetPlanRepository.findByQuarterYearsWithItems(candidateYears).stream()
                .filter(plan -> isInCalendarYear(plan.getQuarter(), plan.getMonth(), year))
                .toList();
        List<StudyDepositLedgerEntry> ledgerEntries = ledgerRepository.findDetailByQuarterYears(candidateYears).stream()
                .filter(entry -> isInCalendarYear(entry.getQuarter(), entry.getMonth(), year))
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Styles styles = new Styles(workbook);
            writePlanSheet(workbook, styles, year, plans);
            writeDepositSheet(workbook, styles, ledgerEntries);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("가계부 엑셀 생성에 실패했습니다.", e);
        }
    }

    // ---------- 시트 1: 예산안 ----------

    private void writePlanSheet(Workbook workbook, Styles styles, int year, List<BudgetPlan> plans) {
        Sheet sheet = workbook.createSheet("예산안");
        Map<BudgetCategory, long[]> planned = aggregate(plans, BudgetItem::getPlannedAmount);
        Map<BudgetCategory, long[]> actual = aggregate(plans, BudgetItem::getActualAmount);

        int rowIdx = 0;
        Row titleRow = sheet.createRow(rowIdx++);
        setText(titleRow, COL_LABEL, year + "년 가계부", styles.title);

        Row headerRow = sheet.createRow(rowIdx++);
        setText(headerRow, COL_LABEL, "항목", styles.header);
        for (int m = 1; m <= MONTH_COUNT; m++) {
            setText(headerRow, COL_FIRST_MONTH + m - 1, m + "월", styles.header);
        }
        setText(headerRow, COL_TOTAL, "합계", styles.header);

        rowIdx = writeBlock(sheet, styles, rowIdx, "예상", planned);
        sheet.createRow(rowIdx++); // 예상/실제 블록 구분용 빈 행
        writeBlock(sheet, styles, rowIdx, "실제", actual);

        sheet.setColumnWidth(COL_LABEL, 26 * 256);
        for (int c = COL_FIRST_MONTH; c <= COL_TOTAL; c++) {
            sheet.setColumnWidth(c, 14 * 256);
        }
        sheet.createFreezePane(1, 2);
    }

    /** 수입/지출 전체 블록(예상 또는 실제)을 쓰고, 다음에 쓸 행 번호를 반환한다. */
    private int writeBlock(Sheet sheet, Styles styles, int rowIdx, String prefix,
                            Map<BudgetCategory, long[]> amounts) {
        setText(sheet.createRow(rowIdx++), COL_LABEL, prefix + " 수입", styles.groupHeader);
        long[] incomeSubtotal = new long[MONTH_COUNT];
        for (BudgetSheetLayout.CategoryRow row : BudgetSheetLayout.INCOME.rows()) {
            long[] monthly = amounts.getOrDefault(row.category(), new long[MONTH_COUNT]);
            writeAmountRow(sheet, styles, rowIdx++, row.label(), monthly, styles.amount);
            addInto(incomeSubtotal, monthly);
        }
        writeAmountRow(sheet, styles, rowIdx++, "소계", incomeSubtotal, styles.amountBold);

        setText(sheet.createRow(rowIdx++), COL_LABEL, prefix + " 지출", styles.groupHeader);
        long[] expenseTotal = new long[MONTH_COUNT];
        for (BudgetSheetLayout.Group group : BudgetSheetLayout.EXPENSE_GROUPS) {
            setText(sheet.createRow(rowIdx++), COL_LABEL, group.label(), styles.subGroupHeader);
            long[] groupSubtotal = new long[MONTH_COUNT];
            for (BudgetSheetLayout.CategoryRow row : group.rows()) {
                long[] monthly = amounts.getOrDefault(row.category(), new long[MONTH_COUNT]);
                writeAmountRow(sheet, styles, rowIdx++, row.label(), monthly, styles.amount);
                addInto(groupSubtotal, monthly);
            }
            writeAmountRow(sheet, styles, rowIdx++, "소계", groupSubtotal, styles.amountBold);
            addInto(expenseTotal, groupSubtotal);
        }

        writeAmountRow(sheet, styles, rowIdx++, prefix + " 총 지출", expenseTotal, styles.amountBold);

        long[] margin = new long[MONTH_COUNT];
        for (int i = 0; i < MONTH_COUNT; i++) {
            // 지출은 음수로 저장되므로 그대로 더하면 수입 - 지출이 된다
            margin[i] = incomeSubtotal[i] + expenseTotal[i];
        }
        writeAmountRow(sheet, styles, rowIdx++, prefix + " 마진", margin, styles.amountBold);
        return rowIdx;
    }

    private boolean isInCalendarYear(Quarter quarter, Integer month, int year) {
        if (month == null || month < 1 || month > MONTH_COUNT) return false;
        return BudgetCalendar.toYearMonth(quarter, month).getYear() == year;
    }

    /** 카테고리별 월(1~12) 금액 집계. 같은 연도에 분기가 여러 개면 같은 월끼리 합산한다. */
    private Map<BudgetCategory, long[]> aggregate(List<BudgetPlan> plans,
                                                   Function<BudgetItem, Long> amountGetter) {
        Map<BudgetCategory, long[]> result = new EnumMap<>(BudgetCategory.class);
        for (BudgetPlan plan : plans) {
            Integer month = plan.getMonth();
            if (month == null || month < 1 || month > MONTH_COUNT) continue;
            for (BudgetItem item : plan.getItems()) {
                Long amount = amountGetter.apply(item);
                if (amount == null) continue;
                result.computeIfAbsent(item.getCategory(), c -> new long[MONTH_COUNT])[month - 1] += amount;
            }
        }
        return result;
    }

    private void writeAmountRow(Sheet sheet, Styles styles, int rowIdx, String label,
                                 long[] monthly, CellStyle amountStyle) {
        Row row = sheet.createRow(rowIdx);
        setText(row, COL_LABEL, label, styles.label);
        long total = 0;
        for (int i = 0; i < MONTH_COUNT; i++) {
            setAmount(row, COL_FIRST_MONTH + i, monthly[i], amountStyle);
            total += monthly[i];
        }
        setAmount(row, COL_TOTAL, total, styles.amountBold);
    }

    // ---------- 시트 2: 수입지출 총액 (스터디 보증금 건별 내역) ----------

    private void writeDepositSheet(Workbook workbook, Styles styles,
                                    List<StudyDepositLedgerEntry> entries) {
        Sheet sheet = workbook.createSheet("수입지출 총액");

        Row titleRow = sheet.createRow(0);
        setText(titleRow, 0, "스터디 보증금 내역", styles.title);
        setText(titleRow, 9, "분기", styles.header);
        setText(titleRow, 10, "스터디 보증금 총액", styles.header);
        setText(titleRow, 11, "스터디 보증금 환불 총액", styles.header);

        Row headerRow = sheet.createRow(1);
        String[] headers = {"항목", "예산", "(예상)소계", "실제 내역", "(실제)소계", "차이", "거래 일자", "비고"};
        for (int i = 0; i < headers.length; i++) {
            setText(headerRow, i, headers[i], styles.header);
        }

        // 좌측: 건별 내역 (누계 포함)
        long running = 0;
        int rowIdx = 2;
        for (StudyDepositLedgerEntry entry : entries) {
            ActivityParticipant participant = entry.getActivityParticipant();
            boolean refund = entry.getCategory() == BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND;
            running += entry.getAmount();

            Row row = sheet.createRow(rowIdx++);
            setText(row, 0, "%s %s %s %s".formatted(
                    entry.getQuarter().getName(),
                    participant.getUser().getName(),
                    participant.getActivity().getTitle(),
                    refund ? "환불" : "보증금"), styles.label);
            setAmount(row, 3, entry.getAmount(), styles.amount);
            setAmount(row, 4, running, styles.amount);
            setText(row, 6, entry.getOccurredAt().format(DATE_FORMAT), styles.label);
            setText(row, 7, participant.getUser().getStudentId(), styles.label);
        }

        // 우측: 분기별 요약
        Map<String, long[]> byQuarter = new LinkedHashMap<>();
        for (StudyDepositLedgerEntry entry : entries) {
            long[] sums = byQuarter.computeIfAbsent(entry.getQuarter().getName(), q -> new long[2]);
            if (entry.getCategory() == BudgetCategory.INCOME_STUDY_DEPOSIT) {
                sums[0] += entry.getAmount();
            } else {
                sums[1] += entry.getAmount();
            }
        }
        int summaryRowIdx = 1;
        for (Map.Entry<String, long[]> summary : byQuarter.entrySet()) {
            Row row = sheet.getRow(summaryRowIdx);
            if (row == null) row = sheet.createRow(summaryRowIdx);
            summaryRowIdx++;
            setText(row, 9, summary.getKey(), styles.label);
            setAmount(row, 10, summary.getValue()[0], styles.amount);
            setAmount(row, 11, summary.getValue()[1], styles.amount);
        }

        sheet.setColumnWidth(0, 40 * 256);
        for (int c = 1; c <= 7; c++) {
            sheet.setColumnWidth(c, 14 * 256);
        }
        for (int c = 9; c <= 11; c++) {
            sheet.setColumnWidth(c, 20 * 256);
        }
    }

    // ---------- 셀 유틸 ----------

    private void setText(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value == null ? "" : value);
        cell.setCellStyle(style);
    }

    private void setAmount(Row row, int col, long value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void addInto(long[] target, long[] source) {
        for (int i = 0; i < MONTH_COUNT; i++) {
            target[i] += source[i];
        }
    }

    /** 워크북 단위로 재사용하는 셀 스타일 묶음 (POI는 스타일 개수 상한이 있어 매 셀 생성 금지) */
    private static final class Styles {
        private final CellStyle title;
        private final CellStyle header;
        private final CellStyle groupHeader;
        private final CellStyle subGroupHeader;
        private final CellStyle label;
        private final CellStyle amount;
        private final CellStyle amountBold;

        private Styles(Workbook workbook) {
            Font bold = workbook.createFont();
            bold.setBold(true);
            short currencyFormat = workbook.createDataFormat().getFormat("₩#,##0;[Red]-₩#,##0");

            this.title = workbook.createCellStyle();
            this.title.setFont(bold);

            this.header = workbook.createCellStyle();
            this.header.setFont(bold);

            this.groupHeader = workbook.createCellStyle();
            this.groupHeader.setFont(bold);

            this.subGroupHeader = workbook.createCellStyle();
            this.subGroupHeader.setFont(bold);

            this.label = workbook.createCellStyle();

            this.amount = workbook.createCellStyle();
            this.amount.setDataFormat(currencyFormat);

            this.amountBold = workbook.createCellStyle();
            this.amountBold.setDataFormat(currencyFormat);
            this.amountBold.setFont(bold);
        }
    }
}
