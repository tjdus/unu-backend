package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.quarter.dto.CurrentQuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@RestController
@RequestMapping("/api/public/current-quarter")
@RequiredArgsConstructor
public class CurrentQuarterPublicController {
    private final CurrentQuarterService currentQuarterService;

    @GetMapping("")
    public ResponseEntity<QuarterResponseDto> getCurrentQuarter() {
        return ResponseEntity.ok(currentQuarterService.get());
    }

}
