package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.budget.dto.StudyDepositLedgerEntryDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budget/deposit-entries")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class StudyDepositLedgerController {

    private final StudyDepositLedgerService studyDepositLedgerService;

    @GetMapping
    public ResponseEntity<List<StudyDepositLedgerEntryDto>> getEntries(
            @RequestParam UUID quarterId,
            @RequestParam Integer month,
            @RequestParam BudgetCategory category
    ) {
        return ResponseEntity.ok(studyDepositLedgerService.getDetail(quarterId, month, category));
    }
}
