package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/budget/export")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class BudgetExportController {

    private static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final BudgetExportService budgetExportService;

    @GetMapping
    public ResponseEntity<byte[]> export(@RequestParam int year) {
        byte[] body = budgetExportService.exportYear(year);

        // 한글 파일명은 RFC 5987 형식으로 인코딩해야 브라우저에서 깨지지 않는다
        String filename = URLEncoder.encode(year + "년_가계부.xlsx", StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType(XLSX_CONTENT_TYPE))
                .body(body);
    }
}
