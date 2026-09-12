package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.budget.dto.BudgetExpenseEntryDto;
import sogang.cnu.backend.budget.dto.BudgetExpenseEntryRequestDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget/expense-entries")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class BudgetExpenseEntryController {

    private final BudgetExpenseEntryService budgetExpenseEntryService;

    @GetMapping
    public ResponseEntity<List<BudgetExpenseEntryDto>> getEntries(
            @RequestParam UUID quarterId,
            @RequestParam Integer month,
            @RequestParam BudgetCategory category
    ) {
        return ResponseEntity.ok(budgetExpenseEntryService.getEntries(quarterId, month, category));
    }

    @PostMapping
    public ResponseEntity<BudgetExpenseEntryDto> create(@RequestBody BudgetExpenseEntryRequestDto request) {
        return ResponseEntity.ok(budgetExpenseEntryService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetExpenseEntryDto> update(
            @PathVariable UUID id,
            @RequestBody BudgetExpenseEntryRequestDto request
    ) {
        return ResponseEntity.ok(budgetExpenseEntryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        budgetExpenseEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
