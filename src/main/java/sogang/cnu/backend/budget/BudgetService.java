package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.budget.dto.BudgetItemRequestDto;
import sogang.cnu.backend.budget.dto.BudgetPlanRequestDto;
import sogang.cnu.backend.budget.dto.BudgetPlanResponseDto;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;

import java.util.List;
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
                .orElseThrow(() -> new IllegalArgumentException("예산 계획을 찾을 수 없습니다."));
        return BudgetPlanResponseDto.from(plan);
    }

    // 예산 계획 단건 조회
    public BudgetPlanResponseDto getById(UUID id) {
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예산 계획을 찾을 수 없습니다."));
        return BudgetPlanResponseDto.from(plan);
    }

    // 예산 계획 생성 (월별 항목 포함)
    @Transactional
    public BudgetPlanResponseDto create(BudgetPlanRequestDto dto) {
        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new IllegalArgumentException("분기를 찾을 수 없습니다."));

        // 이미 해당 월 계획이 있으면 예외
        if (budgetPlanRepository.findByQuarterIdAndMonth(dto.getQuarterId(), dto.getMonth()).isPresent()) {
            throw new IllegalStateException("해당 분기/월의 예산 계획이 이미 존재합니다.");
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
        BudgetPlan plan = budgetPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예산 계획을 찾을 수 없습니다."));

        plan.update(dto.getNote());

        // 기존 항목 전체 삭제 후 재등록
        budgetItemRepository.deleteByBudgetPlanId(id);
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
                .orElseThrow(() -> new IllegalArgumentException("예산 계획을 찾을 수 없습니다."));
        budgetPlanRepository.delete(plan);
    }

    // 전월 이월금 자동 계산: 전달 실제 마진을 반환
    public Long getPreviousMonthCarryover(UUID quarterId, Integer month) {
        int prevMonth = month - 1;
        UUID targetQuarterId = quarterId;

        // 1월이면 이전 분기의 마지막 달을 찾아야 하나 일단 0 반환
        if (prevMonth < 1) {
            return 0L;
        }

        return budgetPlanRepository.findByQuarterIdAndMonth(targetQuarterId, prevMonth)
                .map(plan -> {
                    long actualIncome = plan.getItems().stream()
                            .filter(i -> i.getCategory().name().startsWith("INCOME_") && i.getActualAmount() != null)
                            .mapToLong(BudgetItem::getActualAmount).sum();
                    long actualExpense = plan.getItems().stream()
                            .filter(i -> i.getCategory().name().startsWith("EXPENSE_") && i.getActualAmount() != null)
                            .mapToLong(i -> Math.abs(i.getActualAmount())).sum();
                    return actualIncome - actualExpense;
                })
                .orElse(0L);
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
