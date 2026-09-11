package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.budget.dto.BudgetImportResultDto;
import sogang.cnu.backend.budget.dto.BudgetImportResultDto.Change;
import sogang.cnu.backend.budget.dto.BudgetImportResultDto.MonthResult;
import sogang.cnu.backend.budget.dto.BudgetImportResultDto.Status;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BudgetExportService가 만든 '예산안' 시트를 다시 읽어 가계부에 반영한다.
 * 미리보기(preview)는 저장 없이 월별 변경 내역만 계산하고, 적용(apply)은 같은 계산 후
 * 오류가 하나라도 있으면 아무것도 저장하지 않는다(all-or-nothing).
 */
@Service
@RequiredArgsConstructor
public class BudgetImportService {

    private static final String PLAN_SHEET = "예산안";
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\d{4})년");
    private static final int MONTH_COUNT = 12;
    private static final int COL_FIRST_MONTH = 1;       // B열 = 1월
    private static final int HEADER_SEARCH_ROWS = 10;

    // 보증금 원장에서 자동 계산되는 실제금액 — 화면 편집 모달처럼 업로드로도 덮어쓰지 않는다
    private static final Set<BudgetCategory> AUTO_SYNCED = EnumSet.of(
            BudgetCategory.INCOME_STUDY_DEPOSIT,
            BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND
    );

    private static final List<BudgetSheetLayout.CategoryRow> CATEGORY_ROWS = flattenLayout();
    private static final Map<String, BudgetCategory> CATEGORY_BY_LABEL = new LinkedHashMap<>();
    private static final Map<BudgetCategory, String> LABEL_BY_CATEGORY = new EnumMap<>(BudgetCategory.class);
    private static final Map<BudgetCategory, Integer> DISPLAY_ORDER = new EnumMap<>(BudgetCategory.class);
    private static final Set<String> IGNORED_LABELS = new HashSet<>();

    static {
        for (int i = 0; i < CATEGORY_ROWS.size(); i++) {
            BudgetSheetLayout.CategoryRow row = CATEGORY_ROWS.get(i);
            CATEGORY_BY_LABEL.put(row.label(), row.category());
            LABEL_BY_CATEGORY.put(row.category(), row.label());
            DISPLAY_ORDER.put(row.category(), i + 1);
        }
        // 계산 행·그룹 헤더는 가져오지 않는다
        IGNORED_LABELS.add("소계");
        for (Block block : Block.values()) {
            IGNORED_LABELS.add(block.label + " 총 지출");
            IGNORED_LABELS.add(block.label + " 마진");
        }
        BudgetSheetLayout.EXPENSE_GROUPS.forEach(group -> IGNORED_LABELS.add(group.label()));
    }

    private final BudgetPlanRepository budgetPlanRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final QuarterRepository quarterRepository;

    @Transactional(readOnly = true)
    public BudgetImportResultDto preview(MultipartFile file) {
        return buildPlan(parse(file)).result();
    }

    @Transactional
    public BudgetImportResultDto apply(MultipartFile file) {
        ImportPlan plan = buildPlan(parse(file));
        List<String> errors = plan.result().errors();
        if (!errors.isEmpty()) {
            throw new BadRequestException("엑셀에 오류가 있어 반영하지 않았습니다. " + String.join(" / ", errors));
        }
        plan.writes().forEach(this::write);
        return plan.result();
    }

    // ---------- 파싱 ----------

    private enum Block {
        PLANNED("예상"), ACTUAL("실제");

        private final String label;

        Block(String label) {
            this.label = label;
        }
    }

    private record ParsedSheet(
            int year,
            Map<Block, Map<BudgetCategory, long[]>> values,
            List<String> warnings,
            List<String> errors
    ) {
        /** 해당 블록에 카테고리 행이 없으면 null (그 칸은 반영하지 않는다) */
        Long value(Block block, BudgetCategory category, int month) {
            long[] monthly = values.getOrDefault(block, Map.of()).get(category);
            return monthly == null ? null : monthly[month - 1];
        }
    }

    private ParsedSheet parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("업로드할 파일이 비어 있습니다.");
        }
        try (Workbook workbook = openWorkbook(file)) {
            return parseWorkbook(workbook);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Workbook openWorkbook(MultipartFile file) {
        try {
            return WorkbookFactory.create(file.getInputStream());
        } catch (IOException | RuntimeException e) {
            throw new BadRequestException("엑셀 파일을 열 수 없습니다. 가계부에서 내려받은 .xlsx 양식인지 확인해주세요.");
        }
    }

    private ParsedSheet parseWorkbook(Workbook workbook) {
        List<String> warnings = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        Sheet sheet = workbook.getSheet(PLAN_SHEET);
        if (sheet == null) {
            errors.add("'" + PLAN_SHEET + "' 시트를 찾을 수 없습니다.");
            return new ParsedSheet(0, Map.of(), warnings, errors);
        }

        int headerRow = findHeaderRow(sheet);
        int year = findYear(sheet, headerRow < 0 ? HEADER_SEARCH_ROWS : headerRow);
        if (year == 0) {
            errors.add("제목 셀에서 연도를 찾을 수 없습니다. (예: '2026년 가계부')");
        }
        if (headerRow < 0) {
            errors.add("'항목 / 1월 … 12월' 헤더 행을 찾을 수 없습니다.");
        }
        if (!errors.isEmpty()) {
            return new ParsedSheet(year, Map.of(), warnings, errors);
        }

        Map<Block, Map<BudgetCategory, long[]>> values = new EnumMap<>(Block.class);
        for (Block block : Block.values()) {
            values.put(block, new EnumMap<>(BudgetCategory.class));
        }

        Block current = null;
        for (int r = headerRow + 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            String label = text(row.getCell(0));
            if (label.isEmpty()) continue;

            Block marker = blockMarker(label);
            if (marker != null) {
                current = marker;
                continue;
            }
            BudgetCategory category = CATEGORY_BY_LABEL.get(label);
            if (category == null) {
                if (!IGNORED_LABELS.contains(label)) {
                    warnings.add("알 수 없는 항목 행은 무시했습니다: '" + label + "' (" + (r + 1) + "행)");
                }
                continue;
            }
            if (current == null) {
                warnings.add("예상/실제 구분 전에 나온 항목 행은 무시했습니다: '" + label + "' (" + (r + 1) + "행)");
                continue;
            }
            Map<BudgetCategory, long[]> blockValues = values.get(current);
            if (blockValues.containsKey(category)) {
                warnings.add(current.label + " 블록에 '" + label + "' 행이 중복되어 첫 번째 행만 반영합니다.");
                continue;
            }
            long[] monthly = new long[MONTH_COUNT];
            for (int m = 0; m < MONTH_COUNT; m++) {
                Long amount = readAmount(row.getCell(COL_FIRST_MONTH + m), errors);
                if (amount != null) monthly[m] = amount;
            }
            blockValues.put(category, monthly);
        }

        for (Block block : Block.values()) {
            Map<BudgetCategory, long[]> blockValues = values.get(block);
            if (blockValues.isEmpty()) {
                warnings.add(block.label + " 블록이 없어 " + block.label + "금액은 반영하지 않습니다.");
                continue;
            }
            for (BudgetSheetLayout.CategoryRow row : CATEGORY_ROWS) {
                if (!blockValues.containsKey(row.category())) {
                    warnings.add(block.label + " 블록에 '" + row.label() + "' 행이 없어 해당 칸은 반영하지 않습니다.");
                }
            }
        }
        return new ParsedSheet(year, values, warnings, errors);
    }

    private int findHeaderRow(Sheet sheet) {
        int last = Math.min(sheet.getLastRowNum(), HEADER_SEARCH_ROWS);
        for (int r = 0; r <= last; r++) {
            Row row = sheet.getRow(r);
            if (row == null || !"항목".equals(text(row.getCell(0)))) continue;
            boolean monthsMatch = true;
            for (int m = 1; m <= MONTH_COUNT; m++) {
                if (!(m + "월").equals(text(row.getCell(COL_FIRST_MONTH + m - 1)))) {
                    monthsMatch = false;
                    break;
                }
            }
            if (monthsMatch) return r;
        }
        return -1;
    }

    private int findYear(Sheet sheet, int beforeRow) {
        for (int r = 0; r < beforeRow && r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;
            Matcher matcher = YEAR_PATTERN.matcher(text(row.getCell(0)));
            if (matcher.find()) return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private Block blockMarker(String label) {
        for (Block block : Block.values()) {
            if (label.equals(block.label + " 수입") || label.equals(block.label + " 지출")) {
                return block;
            }
        }
        return null;
    }

    /** 숫자 / 수식(캐시된 결과) / '₩30,000' 같은 문자열 / 빈 칸(=0)을 읽는다. 읽을 수 없으면 오류를 남기고 null. */
    private Long readAmount(Cell cell, List<String> errors) {
        if (cell == null) return 0L;
        CellType type = cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
        return switch (type) {
            case BLANK -> 0L;
            case NUMERIC -> Math.round(cell.getNumericCellValue());
            case STRING -> parseAmountText(cell, errors);
            default -> {
                errors.add(address(cell) + ": 금액으로 읽을 수 없는 값입니다.");
                yield null;
            }
        };
    }

    private Long parseAmountText(Cell cell, List<String> errors) {
        String raw = cell.getStringCellValue();
        String cleaned = raw.replaceAll("[₩,\\s]", "");
        if (cleaned.isEmpty()) return 0L;
        try {
            double value = Double.parseDouble(cleaned);
            if (Double.isFinite(value)) return Math.round(value);
        } catch (NumberFormatException ignored) {
            // 아래에서 오류로 기록
        }
        errors.add(address(cell) + ": 숫자가 아닙니다 ('" + raw.trim() + "')");
        return null;
    }

    private static String text(Cell cell) {
        return cell == null ? "" : new DataFormatter().formatCellValue(cell).trim();
    }

    private static String address(Cell cell) {
        return new CellReference(cell).formatAsString(false);
    }

    // ---------- 월별 변경 계산 ----------

    private record ImportPlan(BudgetImportResultDto result, List<MonthWrite> writes) {}

    /** existingPlan이 없으면 newPlanQuarter에 새 계획을 만든다. changed: 카테고리 → [예상, 실제] */
    private record MonthWrite(
            BudgetPlan existingPlan,
            Quarter newPlanQuarter,
            int month,
            Map<BudgetCategory, long[]> changed
    ) {}

    private ImportPlan buildPlan(ParsedSheet parsed) {
        List<String> warnings = new ArrayList<>(parsed.warnings());
        List<String> errors = new ArrayList<>(parsed.errors());
        int year = parsed.year();
        if (!errors.isEmpty()) {
            return new ImportPlan(new BudgetImportResultDto(year, List.of(), warnings, errors), List.of());
        }

        // 겨울학기 때문에 달력 연도 Y의 계획은 quarter.year가 Y-1인 분기에도 있다
        List<BudgetPlan> candidatePlans = budgetPlanRepository.findByQuarterYearsWithItems(List.of(year - 1, year));
        List<Quarter> quarters = quarterRepository.findAll();

        List<MonthResult> months = new ArrayList<>();
        List<MonthWrite> writes = new ArrayList<>();
        for (int m = 1; m <= MONTH_COUNT; m++) {
            YearMonth yearMonth = YearMonth.of(year, m);
            BudgetPlan existing = findExistingPlan(candidatePlans, yearMonth, warnings);
            Map<BudgetCategory, BudgetItem> existingItems = existing == null ? Map.of() : itemsByCategory(existing);

            warnAutoSyncedMismatch(parsed, m, existingItems, warnings);

            if (!hasData(parsed, m)) {
                months.add(new MonthResult(m, Status.SKIP, quarterName(existing), List.of()));
                continue;
            }

            Quarter target = existing != null ? existing.getQuarter() : findCoveringQuarter(quarters, yearMonth, warnings);
            if (target == null) {
                errors.add(m + "월에 해당하는 분기가 등록되어 있지 않아 반영할 수 없습니다. 분기를 먼저 등록해주세요.");
                months.add(new MonthResult(m, Status.ERROR, null, List.of()));
                continue;
            }

            List<Change> changes = new ArrayList<>();
            Map<BudgetCategory, long[]> changed = new EnumMap<>(BudgetCategory.class);
            for (BudgetSheetLayout.CategoryRow row : CATEGORY_ROWS) {
                BudgetCategory category = row.category();
                BudgetItem item = existingItems.get(category);
                long beforePlanned = amountOrZero(item == null ? null : item.getPlannedAmount());
                long beforeActual = amountOrZero(item == null ? null : item.getActualAmount());

                Long filePlanned = parsed.value(Block.PLANNED, category, m);
                Long fileActual = parsed.value(Block.ACTUAL, category, m);
                long afterPlanned = filePlanned == null ? beforePlanned : normalize(category, filePlanned);
                long afterActual = AUTO_SYNCED.contains(category) || fileActual == null
                        ? beforeActual
                        : normalize(category, fileActual);

                if (afterPlanned != beforePlanned) {
                    changes.add(new Change(row.label(), Block.PLANNED.label, beforePlanned, afterPlanned));
                }
                if (afterActual != beforeActual) {
                    changes.add(new Change(row.label(), Block.ACTUAL.label, beforeActual, afterActual));
                }
                if (afterPlanned != beforePlanned || afterActual != beforeActual) {
                    changed.put(category, new long[]{afterPlanned, afterActual});
                }
            }

            months.add(new MonthResult(m, existing != null ? Status.UPDATE : Status.CREATE, target.getName(), changes));
            if (!changed.isEmpty()) {
                writes.add(new MonthWrite(existing, existing == null ? target : null, m, changed));
            }
        }
        return new ImportPlan(new BudgetImportResultDto(year, months, warnings, errors), writes);
    }

    /** 예상금액이나 (자동 연동이 아닌) 실제금액 중 0이 아닌 값이 하나라도 있으면 반영 대상 월 */
    private boolean hasData(ParsedSheet parsed, int month) {
        for (BudgetSheetLayout.CategoryRow row : CATEGORY_ROWS) {
            Long planned = parsed.value(Block.PLANNED, row.category(), month);
            if (planned != null && planned != 0) return true;
            if (AUTO_SYNCED.contains(row.category())) continue;
            Long actual = parsed.value(Block.ACTUAL, row.category(), month);
            if (actual != null && actual != 0) return true;
        }
        return false;
    }

    private void warnAutoSyncedMismatch(ParsedSheet parsed, int month,
                                        Map<BudgetCategory, BudgetItem> existingItems, List<String> warnings) {
        for (BudgetCategory category : AUTO_SYNCED) {
            Long fileActual = parsed.value(Block.ACTUAL, category, month);
            if (fileActual == null) continue;
            long fileValue = normalize(category, fileActual);
            BudgetItem item = existingItems.get(category);
            long systemValue = amountOrZero(item == null ? null : item.getActualAmount());
            if (fileValue != systemValue) {
                warnings.add(month + "월 '" + LABEL_BY_CATEGORY.get(category)
                        + "' 실제금액은 보증금 신청/수료 기록에서 자동 계산되는 값이라 반영하지 않습니다. (파일 "
                        + won(fileValue) + " / 시스템 " + won(systemValue) + ")");
            }
        }
    }

    private BudgetPlan findExistingPlan(List<BudgetPlan> candidates, YearMonth yearMonth, List<String> warnings) {
        List<BudgetPlan> matches = candidates.stream()
                .filter(plan -> plan.getMonth() != null && plan.getMonth() >= 1 && plan.getMonth() <= MONTH_COUNT)
                .filter(plan -> BudgetCalendar.toYearMonth(plan.getQuarter(), plan.getMonth()).equals(yearMonth))
                .toList();
        if (matches.isEmpty()) return null;
        BudgetPlan chosen = matches.stream()
                .filter(plan -> BudgetCalendar.covers(plan.getQuarter(), yearMonth))
                .findFirst()
                .orElse(matches.get(0));
        if (matches.size() > 1) {
            warnings.add(yearMonth.getMonthValue() + "월 계획이 여러 분기에 있어 '"
                    + chosen.getQuarter().getName() + "' 계획만 수정합니다.");
        }
        return chosen;
    }

    private Quarter findCoveringQuarter(List<Quarter> quarters, YearMonth yearMonth, List<String> warnings) {
        List<Quarter> covering = quarters.stream()
                .filter(quarter -> BudgetCalendar.covers(quarter, yearMonth))
                .toList();
        if (covering.isEmpty()) return null;
        if (covering.size() > 1) {
            warnings.add(yearMonth.getMonthValue() + "월을 포함하는 분기가 여러 개라 '"
                    + covering.get(0).getName() + "'에 새 계획을 만듭니다.");
        }
        return covering.get(0);
    }

    // ---------- 저장 ----------

    private void write(MonthWrite monthWrite) {
        BudgetPlan plan = monthWrite.existingPlan() != null
                ? monthWrite.existingPlan()
                : budgetPlanRepository.save(BudgetPlan.builder()
                        .quarter(monthWrite.newPlanQuarter())
                        .month(monthWrite.month())
                        .build());
        Map<BudgetCategory, BudgetItem> items = itemsByCategory(plan);
        monthWrite.changed().forEach((category, values) -> {
            BudgetItem item = items.get(category);
            if (item != null) {
                item.update(values[0], values[1], item.getNote(), item.getDisplayOrder());
            } else {
                budgetItemRepository.save(BudgetItem.builder()
                        .budgetPlan(plan)
                        .category(category)
                        .plannedAmount(values[0])
                        .actualAmount(values[1])
                        .displayOrder(DISPLAY_ORDER.get(category))
                        .build());
            }
        });
    }

    // ---------- 유틸 ----------

    private static List<BudgetSheetLayout.CategoryRow> flattenLayout() {
        List<BudgetSheetLayout.CategoryRow> rows = new ArrayList<>(BudgetSheetLayout.INCOME.rows());
        BudgetSheetLayout.EXPENSE_GROUPS.forEach(group -> rows.addAll(group.rows()));
        return List.copyOf(rows);
    }

    private static Map<BudgetCategory, BudgetItem> itemsByCategory(BudgetPlan plan) {
        Map<BudgetCategory, BudgetItem> items = new EnumMap<>(BudgetCategory.class);
        plan.getItems().forEach(item -> items.putIfAbsent(item.getCategory(), item));
        return items;
    }

    /** 지출은 DB에 음수로 저장한다. 총무가 엑셀에 양수로 적어도 지출로 처리. */
    private static long normalize(BudgetCategory category, long value) {
        return category.name().startsWith("EXPENSE_") ? -Math.abs(value) : value;
    }

    private static long amountOrZero(Long amount) {
        return amount == null ? 0L : amount;
    }

    private static String quarterName(BudgetPlan plan) {
        return plan == null ? null : plan.getQuarter().getName();
    }

    private static String won(long amount) {
        return "₩" + NumberFormat.getNumberInstance(Locale.KOREA).format(amount);
    }
}
