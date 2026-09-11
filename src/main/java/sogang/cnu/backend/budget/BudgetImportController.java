package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.budget.dto.BudgetImportResultDto;

@RestController
@RequestMapping("/api/budget/import")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class BudgetImportController {

    private final BudgetImportService budgetImportService;

    // 저장하지 않고 월별 변경 내역만 계산한다
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BudgetImportResultDto> preview(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(budgetImportService.preview(file));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BudgetImportResultDto> apply(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(budgetImportService.apply(file));
    }
}
