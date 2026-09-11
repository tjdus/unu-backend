package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.budget.dto.BudgetPlanRequestDto;
import sogang.cnu.backend.budget.dto.BudgetPlanResponseDto;

import java.util.List;
import java.util.UUID;

// 학회 예산은 총무(운영진) 전용 데이터 — 일반 학회원이 직접 API를 호출해 열람/변경할 수 없어야 한다
@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class BudgetController {

    private final BudgetService budgetService;

    // 분기별 예산 계획 목록 조회
    @GetMapping("")
    public ResponseEntity<List<BudgetPlanResponseDto>> getByQuarter(
            @RequestParam UUID quarterId) {
        return ResponseEntity.ok(budgetService.getByQuarter(quarterId));
    }

    // 특정 월 예산 계획 조회
    @GetMapping("/month")
    public ResponseEntity<BudgetPlanResponseDto> getByQuarterAndMonth(
            @RequestParam UUID quarterId,
            @RequestParam Integer month) {
        return ResponseEntity.ok(budgetService.getByQuarterAndMonth(quarterId, month));
    }

    // 예산 계획 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<BudgetPlanResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(budgetService.getById(id));
    }

    // 예산 계획 생성
    @PostMapping("")
    public ResponseEntity<BudgetPlanResponseDto> create(
            @RequestBody BudgetPlanRequestDto dto) {
        return ResponseEntity.ok(budgetService.create(dto));
    }

    // 예산 계획 수정
    @PutMapping("/{id}")
    public ResponseEntity<BudgetPlanResponseDto> update(
            @PathVariable UUID id,
            @RequestBody BudgetPlanRequestDto dto) {
        return ResponseEntity.ok(budgetService.update(id, dto));
    }

    // 예산 계획 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        budgetService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 전월 이월금 조회
    @GetMapping("/carryover")
    public ResponseEntity<Long> getCarryover(
            @RequestParam UUID quarterId,
            @RequestParam Integer month) {
        return ResponseEntity.ok(budgetService.getPreviousMonthCarryover(quarterId, month));
    }
}
