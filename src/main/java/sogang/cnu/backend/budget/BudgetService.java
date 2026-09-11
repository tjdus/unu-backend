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

    // 분기별 예산 계획 목록 조회
    public List<BudgetPlanResponseDto> getByQuarter(UUID quarterId) {
        return budgetPlanRepository.findByQuarterIdWithItems(quarterId)
                .stream()
                .map(BudgetPlanResponseDto::from)
                .collect(Collectors.toList());
    }

    // 특정 월 예산 계획 조회
    public BudgetPlanResponseDto getByQuarterAndMonth(UUID quarterId, Integer month) {
        BudgetPlan plan = budgetPlanRepository.findByQuarterIdAndMonth(quarterId, month)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        return BudgetPlanResponseDto.from(plan);
    }

    // 예산 계획 단건 조회
    public BudgetPlanResponseDto getById(UUID id) {
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        return BudgetPlanResponseDto.from(plan);
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

        return BudgetPlanResponseDto.from(savedPlan);
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

        return BudgetPlanResponseDto.from(plan);
    }

    // 예산 계획 삭제
    @Transactional
    public void delete(UUID id) {
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("예산 계획을 찾을 수 없습니다."));
        budgetPlanRepository.delete(plan);
    }

    // 전월 이월금 자동 계산: 전달 실제 마진을 반환.
    // 전달이 다른 학기(1월이면 이전 연도 12월)에 속할 수 있으므로 분기가 아닌 연도+월 기준으로 찾는다.
    public Long getPreviousMonthCarryover(UUID quarterId, Integer month) {
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));

        int prevMonth = month - 1;
        int prevYear = quarter.getYear();
        if (prevMonth < 1) {
            prevMonth = 12;
            prevYear -= 1;
        }

        return budgetPlanRepository.findByYearAndMonthWithItems(prevYear, prevMonth).stream()
                .mapToLong(BudgetService::actualMargin)
                .sum();
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

    // 스터디 보증금 등 파생 합계를 특정 카테고리의 actualAmount로 동기화 (find-or-create)
    @Transactional
    public void syncCategoryActualAmount(UUID quarterId, Integer month, BudgetCategory category, long totalAmount) {
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

        item.update(item.getPlannedAmount(), totalAmount, item.getNote(), item.getDisplayOrder());
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
