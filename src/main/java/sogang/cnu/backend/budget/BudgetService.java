package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.budget.dto.BudgetItemRequestDto;
import sogang.cnu.backend.budget.dto.BudgetPlanRequestDto;
import sogang.cnu.backend.budget.dto.BudgetPlanResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetPlanRepository budgetPlanRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final QuarterRepository quarterRepository;
    // 파생 항목(보증금·건별 상세 내역)을 다시 계산할 때 원천 데이터를 직접 읽는다.
    // 각 원천 서비스는 BudgetService를 쓰고 있어서 서비스끼리 참조하면 순환이 된다.
    private final StudyDepositLedgerEntryRepository studyDepositLedgerEntryRepository;
    private final BudgetExpenseEntryRepository budgetExpenseEntryRepository;

    // 분기별 예산 계획 목록 조회
    public List<BudgetPlanResponseDto> getByQuarter(UUID quarterId) {
        return budgetPlanRepository.findByQuarterIdWithItems(quarterId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // 특정 월 예산 계획 조회
    public BudgetPlanResponseDto getByQuarterAndMonth(UUID quarterId, Integer month) {
        BudgetPlan plan = budgetPlanRepository.findByQuarterIdAndMonth(quarterId, month)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        return toResponse(plan);
    }

    // 예산 계획 단건 조회
    public BudgetPlanResponseDto getById(UUID id) {
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        return toResponse(plan);
    }

    // 예산 계획 생성 (월별 항목 포함)
    @Transactional
    public BudgetPlanResponseDto create(BudgetPlanRequestDto dto) {
        validateNoDuplicateCategory(dto);
        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));

        // 이미 해당 월 계획이 있으면 예외
        if (budgetPlanRepository.findByQuarterIdAndMonth(dto.getQuarterId(), dto.getMonth()).isPresent()) {
            throw new BadRequestException("해당 분기/월의 예산 계획이 이미 존재합니다.");
        }

        BudgetPlan plan = BudgetPlan.builder()
                .quarter(quarter)
                .month(dto.getMonth())
                .note(dto.getNote())
                .build();

        BudgetPlan savedPlan = budgetPlanRepository.save(plan);

        // 항목 저장
        if (dto.getItems() != null) {
            List<BudgetItem> items = dto.getItems().stream()
                    .map(itemDto -> buildItem(savedPlan, itemDto))
                    .collect(Collectors.toList());
            budgetItemRepository.saveAll(items);
            savedPlan.getItems().addAll(items);
        }

        applyDerivedAmounts(savedPlan);
        return toResponse(savedPlan);
    }

    // 예산 계획 수정 (항목 전체 교체)
    @Transactional
    public BudgetPlanResponseDto update(UUID id, BudgetPlanRequestDto dto) {
        validateNoDuplicateCategory(dto);
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));

        plan.update(dto.getNote());

        // 기존 항목 전체 삭제 후 재등록
        // (카테고리 유니크 제약이 있으므로 재등록 INSERT 전에 삭제를 먼저 DB에 반영해야 한다)
        budgetItemRepository.deleteByBudgetPlanId(id);
        budgetItemRepository.flush();
        plan.getItems().clear();

        if (dto.getItems() != null) {
            List<BudgetItem> items = dto.getItems().stream()
                    .map(itemDto -> buildItem(plan, itemDto))
                    .collect(Collectors.toList());
            budgetItemRepository.saveAll(items);
            plan.getItems().addAll(items);
        }

        applyDerivedAmounts(plan);
        return toResponse(plan);
    }

    // 예산 계획 삭제
    @Transactional
    public void delete(UUID id) {
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        budgetPlanRepository.delete(plan);
    }

    // 전월 이월금 자동 계산: 전달 실제 마진을 반환.
    // 전달이 다른 학기(겨울학기, 이전 연도 12월 등)에 속할 수 있으므로 실제 달력 연월 기준으로 찾는다.
    public Long getPreviousMonthCarryover(UUID quarterId, Integer month) {
        if (month == null || month < 1 || month > 12) {
            throw new BadRequestException("월은 1~12 사이여야 합니다.");
        }
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));

        YearMonth prev = BudgetCalendar.toYearMonth(quarter, month).minusMonths(1);
        List<Integer> candidateYears = List.of(prev.getYear() - 1, prev.getYear());

        return budgetPlanRepository.findByQuarterYearsAndMonthWithItems(candidateYears, prev.getMonthValue()).stream()
                .filter(plan -> BudgetCalendar.toYearMonth(plan.getQuarter(), plan.getMonth()).equals(prev))
                .mapToLong(BudgetService::actualMargin)
                .sum();
    }

    /** 응답에 "이 달에 상세 내역으로 잠긴 항목" 목록을 함께 실어 화면이 입력칸을 막을 수 있게 한다 */
    private BudgetPlanResponseDto toResponse(BudgetPlan plan) {
        BudgetPlanResponseDto dto = BudgetPlanResponseDto.from(plan);
        dto.setEntryManagedCategories(budgetExpenseEntryRepository.findCategoriesWithEntries(
                plan.getQuarter().getId(), plan.getMonth()));
        return dto;
    }

    private static long actualMargin(BudgetPlan plan) {
        long actualIncome = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("INCOME_") && i.getActualAmount() != null)
                .mapToLong(BudgetItem::getActualAmount).sum();
        long actualExpense = plan.getItems().stream()
                .filter(i -> i.getCategory().name().startsWith("EXPENSE_") && i.getActualAmount() != null)
                .mapToLong(i -> Math.abs(i.getActualAmount())).sum();
        return actualIncome - actualExpense;
    }

    /**
     * 보증금처럼 예상·실제가 같은 금액인 파생 카테고리 동기화.
     * 총무님 시트도 보증금은 한 줄에 예산(예상)과 실제를 같은 금액으로 적는다 — 받는 순간 금액이 확정되므로
     * 예상을 따로 세우지 않는다. 그래서 두 값을 같이 맞춰 예상 마진과 실제 마진이 어긋나지 않게 한다.
     */
    @Transactional
    public void syncCategoryAmounts(UUID quarterId, Integer month, BudgetCategory category, long totalAmount) {
        syncCategoryAmounts(quarterId, month, category, totalAmount, totalAmount);
    }

    // 파생 합계를 특정 카테고리의 예상·실제 금액에 동기화 (계획/항목이 없으면 생성)
    @Transactional
    public void syncCategoryAmounts(UUID quarterId, Integer month, BudgetCategory category,
                                    long plannedTotal, long actualTotal) {
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));

        BudgetPlan plan = budgetPlanRepository.findByQuarterIdAndMonth(quarterId, month)
                .orElseGet(() -> budgetPlanRepository.save(
                        BudgetPlan.builder().quarter(quarter).month(month).build()));

        BudgetItem item = budgetItemRepository.findFirstByBudgetPlanIdAndCategory(plan.getId(), category)
                .orElseGet(() -> budgetItemRepository.save(
                        BudgetItem.builder()
                                .budgetPlan(plan)
                                .category(category)
                                .plannedAmount(0L)
                                .actualAmount(0L)
                                .displayOrder(null)
                                .build()));

        item.update(plannedTotal, actualTotal, item.getNote(), item.getDisplayOrder());
    }

    /**
     * 계획을 저장한 뒤 파생 카테고리(보증금·건별 상세 내역)의 금액을 원천 데이터에서 다시 계산한다.
     * 화면 편집 모달은 이 항목들의 입력칸을 잠그지만, 엑셀 업로드나 API 직접 호출로도 못 덮어쓰게 하려면
     * 저장 경로에서 한 번 더 맞춰야 한다.
     */
    private void applyDerivedAmounts(BudgetPlan plan) {
        UUID quarterId = plan.getQuarter().getId();
        Integer month = plan.getMonth();

        // 보증금은 항상, 건별 상세 내역은 그 달에 내역이 있을 때만 원천에서 다시 계산한다
        List<BudgetCategory> derived = new java.util.ArrayList<>(BudgetCategory.DEPOSIT_CATEGORIES);
        derived.addAll(budgetExpenseEntryRepository.findCategoriesWithEntries(quarterId, month));

        for (BudgetCategory category : derived) {
            long planned;
            long actual;
            if (BudgetCategory.DEPOSIT_CATEGORIES.contains(category)) {
                planned = studyDepositLedgerEntryRepository.sumAmount(quarterId, month, category);
                actual = planned;
            } else {
                planned = budgetExpenseEntryRepository.sumPlanned(quarterId, month, category);
                actual = budgetExpenseEntryRepository.sumActual(quarterId, month, category);
            }

            BudgetItem item = plan.getItems().stream()
                    .filter(i -> i.getCategory() == category)
                    .findFirst()
                    .orElse(null);
            if (item == null) {
                // 원천 데이터도 없으면 빈 항목을 만들지 않는다
                if (planned == 0 && actual == 0) continue;
                item = budgetItemRepository.save(BudgetItem.builder()
                        .budgetPlan(plan)
                        .category(category)
                        .plannedAmount(planned)
                        .actualAmount(actual)
                        .build());
                plan.getItems().add(item);
                continue;
            }
            item.update(planned, actual, item.getNote(), item.getDisplayOrder());
        }
    }

    // 한 달에 같은 카테고리 항목이 두 개 이상 오면 DB 유니크 제약에 걸리므로 미리 걸러 400으로 응답한다
    private void validateNoDuplicateCategory(BudgetPlanRequestDto dto) {
        if (dto.getItems() == null) {
            return;
        }
        Set<BudgetCategory> seen = new HashSet<>();
        for (BudgetItemRequestDto item : dto.getItems()) {
            if (!seen.add(item.getCategory())) {
                throw new BadRequestException("같은 카테고리 항목이 중복되었습니다: " + item.getCategory());
            }
        }
    }

    private BudgetItem buildItem(BudgetPlan plan, BudgetItemRequestDto dto) {
        return BudgetItem.builder()
                .budgetPlan(plan)
                .category(dto.getCategory())
                .plannedAmount(dto.getPlannedAmount())
                .actualAmount(dto.getActualAmount())
                .note(dto.getNote())
                .displayOrder(dto.getDisplayOrder())
                .build();
    }
}
