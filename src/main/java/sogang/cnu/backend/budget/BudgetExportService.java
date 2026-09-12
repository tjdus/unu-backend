package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.quarter.Quarter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 예산안 데이터를 총무의 기존 구글 시트 양식(.xlsx)으로 내보낸다.
 * 시트 구성(총무 시트와 같은 순서): '예산안'(카테고리 행 × 1~12월 열, 예상 블록 + 실제 블록),
 * '실거래'(양식만 — 시스템에 건별 거래 데이터가 없음), '수입지출 총액'(스터디 보증금 건별 내역).
 */
@Service
@RequiredArgsConstructor
public class BudgetExportService {

    private static final int MONTH_COUNT = 12;
    private static final int COL_LABEL = 0;
    private static final int COL_FIRST_MONTH = 1;   // B열 = 1월
    private static final int COL_TOTAL = 13;        // N열 = 합계
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy. M. d");
    private static final int FIRST_ENTRY_ROW = 2;   // 수입지출 총액 시트의 첫 내역 행(0-based) = 엑셀 3행
    private static final int SPARE_ROWS = 5;        // 각 구역 아래 총무가 직접 줄을 적을 여유 칸
    // 총무 시트에 미리 적혀 있는 항목들 (금액은 총무가 엑셀에서 채운다)
    private static final List<String> MT_ITEMS = List.of(
            "mt 장소 1차 결제", "mt 장소 2차 결제", "mt 음료",
            "mt 기타(과자, 얼음, 컵 등)", "mt 배달", "mt 수금", "mt 장소 보증금");
    private static final List<String> GENERAL_MEETING_ITEMS = List.of(
            "개총1차(1학기)", "종총(1학기)", "개총1차(2학기)", "종총(2학기)");
    // 총무 시트 '실거래'/'수입지출 총액' 탭의 열 구성
    private static final String[] TRANSACTION_HEADERS =
            {"항목", "예산", "(예상)소계", "실제 내역", "(실제)소계", "차이", "거래 일자", "비고"};

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
            writeTransactionSheet(workbook, styles);
            writeDepositSheet(workbook, styles, ledgerEntries);
            // 수식 결과를 파일에 함께 저장한다. 엑셀 없이 열어도(업로드 파싱 포함) 값이 보이고,
            // 엑셀에서 금액을 고치면 엑셀이 수식을 다시 계산한다.
            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("예산안 엑셀 생성에 실패했습니다.", e);
        }
    }

    // ---------- 시트 1: 예산안 ----------

    private void writePlanSheet(Workbook workbook, Styles styles, int year, List<BudgetPlan> plans) {
        Sheet sheet = workbook.createSheet("예산안");
        Map<BudgetCategory, long[]> planned = aggregate(plans, BudgetItem::getPlannedAmount);
        Map<BudgetCategory, long[]> actual = aggregate(plans, BudgetItem::getActualAmount);

        int rowIdx = 0;
        Row titleRow = sheet.createRow(rowIdx++);
        setText(titleRow, COL_LABEL, year + "년 예산안", styles.title);

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

    /**
     * 수입/지출 전체 블록(예상 또는 실제)을 쓰고, 다음에 쓸 행 번호를 반환한다.
     * 항목 행만 숫자를 넣고, 소계·총 지출·마진·합계 열은 엑셀 수식으로 넣는다 —
     * 총무가 엑셀에서 금액을 고치면 그 자리에서 다시 계산되도록.
     */
    private int writeBlock(Sheet sheet, Styles styles, int rowIdx, String prefix,
                            Map<BudgetCategory, long[]> amounts) {
        setText(sheet.createRow(rowIdx++), COL_LABEL, prefix + " 수입", styles.groupHeader);
        int incomeFirstRow = rowIdx;
        for (BudgetSheetLayout.CategoryRow row : BudgetSheetLayout.INCOME.rows()) {
            long[] monthly = amounts.getOrDefault(row.category(), new long[MONTH_COUNT]);
            writeAmountRow(sheet, styles, rowIdx++, row.label(), monthly, styles.amount);
        }
        int incomeSubtotalRow = rowIdx;
        writeRangeSumRow(sheet, styles, rowIdx++, "소계", incomeFirstRow, rowIdx - 2);

        setText(sheet.createRow(rowIdx++), COL_LABEL, prefix + " 지출", styles.groupHeader);
        List<Integer> groupSubtotalRows = new ArrayList<>();
        for (BudgetSheetLayout.Group group : BudgetSheetLayout.EXPENSE_GROUPS) {
            setText(sheet.createRow(rowIdx++), COL_LABEL, group.label(), styles.subGroupHeader);
            int groupFirstRow = rowIdx;
            for (BudgetSheetLayout.CategoryRow row : group.rows()) {
                long[] monthly = amounts.getOrDefault(row.category(), new long[MONTH_COUNT]);
                writeAmountRow(sheet, styles, rowIdx++, row.label(), monthly, styles.amount);
            }
            groupSubtotalRows.add(rowIdx);
            writeRangeSumRow(sheet, styles, rowIdx++, "소계", groupFirstRow, rowIdx - 2);
        }

        int expenseTotalRow = rowIdx;
        writeRowsSumRow(sheet, styles, rowIdx++, prefix + " 총 지출", groupSubtotalRows);

        // 지출은 음수로 저장되므로 그대로 더하면 수입 - 지출이 된다
        writeRowsSumRow(sheet, styles, rowIdx++, prefix + " 마진",
                List.of(incomeSubtotalRow, expenseTotalRow));
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

    /** 항목 행: 월 칸은 숫자, 합계 열은 12개월 합 수식 */
    private void writeAmountRow(Sheet sheet, Styles styles, int rowIdx, String label,
                                 long[] monthly, CellStyle amountStyle) {
        Row row = sheet.createRow(rowIdx);
        setText(row, COL_LABEL, label, styles.label);
        for (int i = 0; i < MONTH_COUNT; i++) {
            setAmount(row, COL_FIRST_MONTH + i, monthly[i], amountStyle);
        }
        setRowTotalFormula(row, styles);
    }

    /** 소계 행: 월 칸은 위쪽 연속된 항목 행들의 합 수식 (예: SUM(B4:B6)) */
    private void writeRangeSumRow(Sheet sheet, Styles styles, int rowIdx, String label,
                                   int firstRowIdx, int lastRowIdx) {
        Row row = sheet.createRow(rowIdx);
        setText(row, COL_LABEL, label, styles.label);
        for (int i = 0; i < MONTH_COUNT; i++) {
            String col = columnLetter(COL_FIRST_MONTH + i);
            setFormula(row, COL_FIRST_MONTH + i,
                    "SUM(%s%d:%s%d)".formatted(col, firstRowIdx + 1, col, lastRowIdx + 1),
                    styles.amountBold);
        }
        setRowTotalFormula(row, styles);
    }

    /** 총 지출·마진 행: 떨어져 있는 행들을 더하는 수식 (예: B10+B15+B21) */
    private void writeRowsSumRow(Sheet sheet, Styles styles, int rowIdx, String label,
                                  List<Integer> sourceRowIdxs) {
        Row row = sheet.createRow(rowIdx);
        setText(row, COL_LABEL, label, styles.label);
        for (int i = 0; i < MONTH_COUNT; i++) {
            String col = columnLetter(COL_FIRST_MONTH + i);
            String formula = sourceRowIdxs.stream()
                    .map(src -> col + (src + 1))
                    .collect(Collectors.joining("+"));
            setFormula(row, COL_FIRST_MONTH + i, formula, styles.amountBold);
        }
        setRowTotalFormula(row, styles);
    }

    private void setRowTotalFormula(Row row, Styles styles) {
        int excelRow = row.getRowNum() + 1;
        setFormula(row, COL_TOTAL, "SUM(%s%d:%s%d)".formatted(
                columnLetter(COL_FIRST_MONTH), excelRow,
                columnLetter(COL_FIRST_MONTH + MONTH_COUNT - 1), excelRow), styles.amountBold);
    }

    private String columnLetter(int columnIdx) {
        return String.valueOf((char) ('A' + columnIdx));
    }

    // ---------- 시트 2: 수입지출 총액 (스터디 보증금 건별 내역) ----------

    // ---------- 시트 2: 실거래 (양식만) ----------

    /**
     * 총무 시트의 '실거래' 탭 양식을 빈 표로 만들어 둔다.
     * 시스템은 건별 거래를 저장하지 않으므로 채울 데이터가 없다 — 총무가 엑셀에서 직접 적는 칸이고,
     * 업로드할 때도 이 시트는 읽지 않는다(BudgetImportService는 '예산안' 시트만 본다).
     */
    private void writeTransactionSheet(Workbook workbook, Styles styles) {
        Sheet sheet = workbook.createSheet("실거래");

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < TRANSACTION_HEADERS.length; i++) {
            setText(headerRow, i, TRANSACTION_HEADERS[i], styles.header);
        }

        Row noticeRow = sheet.createRow(2);
        setText(noticeRow, 0,
                "건별 거래 내역은 시스템에서 관리하지 않습니다. 이 시트에 적은 내용은 업로드해도 반영되지 않습니다.",
                styles.label);

        sheet.setColumnWidth(0, 30 * 256);
        for (int c = 1; c <= 6; c++) {
            sheet.setColumnWidth(c, 14 * 256);
        }
        sheet.setColumnWidth(7, 20 * 256);
    }

    // ---------- 시트 3: 수입지출 총액 ----------

    /**
     * 총무 시트처럼 구역(강의형 스터디 / 참여형 스터디 / 인터넷 강의 / MT / 개총&종총 / 기타비용)을
     * 검은 구분선으로 나눠 세로로 쌓는다. 보증금 원장이 있는 두 스터디 구역만 값이 채워지고,
     * 나머지는 시스템에 건별 지출 데이터가 없어 양식(머리글 + 최종 합계)만 만든다.
     */
    private void writeDepositSheet(Workbook workbook, Styles styles,
                                    List<StudyDepositLedgerEntry> entries) {
        Sheet sheet = workbook.createSheet("수입지출 총액");

        List<StudyDepositLedgerEntry> lectureStudy = entries.stream()
                .filter(this::isLectureStudy).toList();
        List<StudyDepositLedgerEntry> participationStudy = entries.stream()
                .filter(entry -> !isLectureStudy(entry)).toList();

        int rowIdx = 0;
        rowIdx = writeDepositSection(sheet, styles, rowIdx, "강의형 스터디", lectureStudy);
        rowIdx = writeDivider(sheet, styles, rowIdx);
        rowIdx = writeDepositSection(sheet, styles, rowIdx, "참여형 스터디", participationStudy);
        rowIdx = writeDivider(sheet, styles, rowIdx);
        rowIdx = writeBlankSection(sheet, styles, rowIdx, "인터넷 강의", List.of());
        rowIdx = writeDivider(sheet, styles, rowIdx);
        rowIdx = writeBlankSection(sheet, styles, rowIdx, "MT", MT_ITEMS);
        rowIdx = writeDivider(sheet, styles, rowIdx);
        rowIdx = writeBlankSection(sheet, styles, rowIdx, "개총&종총", GENERAL_MEETING_ITEMS);
        rowIdx = writeDivider(sheet, styles, rowIdx);
        rowIdx = writeBlankSection(sheet, styles, rowIdx, "기타비용", List.of());

        writeQuarterSummary(sheet, styles, entries, rowIdx);

        sheet.setColumnWidth(0, 40 * 256);
        for (int c = 1; c <= 7; c++) {
            sheet.setColumnWidth(c, 14 * 256);
        }
        for (int c = 9; c <= 11; c++) {
            sheet.setColumnWidth(c, 20 * 256);
        }
    }

    /** 강의형 스터디 = 강의자가 진행하는 유형(활동 유형 '강의'), 그 외 보증금은 참여형으로 본다 */
    private boolean isLectureStudy(StudyDepositLedgerEntry entry) {
        ActivityType activityType = entry.getActivityParticipant().getActivity().getActivityType();
        if (activityType == null) return false;
        String code = activityType.getCode();
        return "SPECIAL_LECTURE".equals(code) || "LECTURE".equals(code);
    }

    /**
     * 보증금 구역: 총무 시트처럼 한 줄에 예산(예상)과 실제를 같은 금액으로 적고,
     * 누계·차이·최종 합계는 수식으로 둔다 — 금액을 고치면 아래 누계와 합계가 따라 바뀐다.
     */
    private int writeDepositSection(Sheet sheet, Styles styles, int rowIdx, String sectionName,
                                     List<StudyDepositLedgerEntry> entries) {
        rowIdx = writeSectionHeader(sheet, styles, rowIdx, sectionName);
        int firstRow = rowIdx;

        for (StudyDepositLedgerEntry entry : entries) {
            ActivityParticipant participant = entry.getActivityParticipant();
            boolean refund = entry.getCategory() == BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND;

            Row row = sheet.createRow(rowIdx++);
            setText(row, 0, "%s %s %s %s".formatted(
                    entry.getQuarter().getName(),
                    participant.getUser().getName(),
                    participant.getActivity().getTitle(),
                    refund ? "환불" : "보증금"), styles.label);
            int excelRow = row.getRowNum() + 1;
            boolean firstEntry = row.getRowNum() == firstRow;
            setAmount(row, 1, entry.getAmount(), styles.amount);                        // 예산
            setFormula(row, 2, firstEntry ? "B%d".formatted(excelRow)                   // (예상)소계 누계
                    : "C%d+B%d".formatted(excelRow - 1, excelRow), styles.amount);
            setAmount(row, 3, entry.getAmount(), styles.amount);                        // 실제 내역
            setFormula(row, 4, firstEntry ? "D%d".formatted(excelRow)                   // (실제)소계 누계
                    : "E%d+D%d".formatted(excelRow - 1, excelRow), styles.amount);
            setFormula(row, 5, "D%d-B%d".formatted(excelRow, excelRow), styles.amount); // 차이
            setText(row, 6, entry.getOccurredAt().format(DATE_FORMAT), styles.label);
            setText(row, 7, participant.getUser().getStudentId(), styles.label);
        }

        rowIdx += SPARE_ROWS;  // 총무가 직접 줄을 추가할 여유 칸
        return writeSectionTotalRow(sheet, styles, rowIdx, firstRow, rowIdx - 1);
    }

    /** 건별 데이터가 없는 구역: 항목 이름만(있으면) 넣고 금액 칸은 비워 둔다 */
    private int writeBlankSection(Sheet sheet, Styles styles, int rowIdx, String sectionName,
                                   List<String> itemLabels) {
        rowIdx = writeSectionHeader(sheet, styles, rowIdx, sectionName);
        int firstRow = rowIdx;
        for (String itemLabel : itemLabels) {
            setText(sheet.createRow(rowIdx++), 0, itemLabel, styles.label);
        }
        rowIdx += SPARE_ROWS;
        return writeSectionTotalRow(sheet, styles, rowIdx, firstRow, rowIdx - 1);
    }

    private int writeSectionHeader(Sheet sheet, Styles styles, int rowIdx, String sectionName) {
        setText(sheet.createRow(rowIdx++), 0, sectionName, styles.title);
        Row headerRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < TRANSACTION_HEADERS.length; i++) {
            setText(headerRow, i, TRANSACTION_HEADERS[i], styles.header);
        }
        return rowIdx;
    }

    /** 구역 맨 아래 '최종 합계' 행 — 예상·실제 열을 각각 합산하는 수식 */
    private int writeSectionTotalRow(Sheet sheet, Styles styles, int rowIdx,
                                      int firstRow, int lastRow) {
        Row row = sheet.createRow(rowIdx++);
        setText(row, 0, "최종 합계", styles.label);
        setFormula(row, 2, "SUM(B%d:B%d)".formatted(firstRow + 1, lastRow + 1), styles.amountBold);
        setFormula(row, 4, "SUM(D%d:D%d)".formatted(firstRow + 1, lastRow + 1), styles.amountBold);
        return rowIdx;
    }

    /** 구역 사이 검은 구분선 (빈 행 - 검은 행 - 빈 행) */
    private int writeDivider(Sheet sheet, Styles styles, int rowIdx) {
        rowIdx++;
        Row dividerRow = sheet.createRow(rowIdx++);
        for (int c = 0; c < TRANSACTION_HEADERS.length; c++) {
            setText(dividerRow, c, "", styles.divider);
        }
        rowIdx++;
        return rowIdx;
    }

    /**
     * 우측 분기별 요약. 항목 이름이 "<분기> <이름> <활동> 보증금|환불" 꼴이라 SUMIFS로 걸러 합산한다.
     * 두 스터디 구역에 흩어져 있어도 시트 전체를 범위로 잡아 한 번에 센다.
     */
    private void writeQuarterSummary(Sheet sheet, Styles styles,
                                      List<StudyDepositLedgerEntry> entries, int lastUsedRowIdx) {
        setText(sheet.getRow(0), 9, "분기", styles.header);
        setText(sheet.getRow(0), 10, "스터디 보증금 총액", styles.header);
        setText(sheet.getRow(0), 11, "스터디 보증금 환불 총액", styles.header);

        List<String> quarterNames = entries.stream()
                .map(entry -> entry.getQuarter().getName())
                .distinct()
                .toList();
        int summaryRowIdx = 1;
        for (String quarterName : quarterNames) {
            Row row = sheet.getRow(summaryRowIdx);
            if (row == null) row = sheet.createRow(summaryRowIdx);
            summaryRowIdx++;
            setText(row, 9, quarterName, styles.label);
            setFormula(row, 10, sumIfsByQuarter(quarterName, "보증금", lastUsedRowIdx), styles.amount);
            setFormula(row, 11, sumIfsByQuarter(quarterName, "환불", lastUsedRowIdx), styles.amount);
        }
    }

    /** 항목 이름이 "<분기> … <접미사>"인 줄의 실제 금액을 합산하는 수식 */
    private String sumIfsByQuarter(String quarterName, String suffix, int lastUsedRowIdx) {
        return "SUMIFS($D$%d:$D$%d,$A$%d:$A$%d,\"%s*%s\")".formatted(
                FIRST_ENTRY_ROW + 1, lastUsedRowIdx + 1, FIRST_ENTRY_ROW + 1, lastUsedRowIdx + 1,
                quarterName, suffix);
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

    private void setFormula(Row row, int col, String formula, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellFormula(formula);
        cell.setCellStyle(style);
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
        private final CellStyle divider;

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

            // 구역 사이 검은 구분선 (총무 시트와 같은 모양)
            this.divider = workbook.createCellStyle();
            this.divider.setFillForegroundColor(IndexedColors.BLACK.getIndex());
            this.divider.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
    }
}
