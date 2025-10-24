package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.quarter.dto.CurrentQuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/current-quarter")
@RequiredArgsConstructor
public class CurrentQuarterController {
    private final CurrentQuarterService currentQuarterService;

    @GetMapping("")
    public ResponseEntity<QuarterResponseDto> getCurrentQuarter() {
        return ResponseEntity.ok(currentQuarterService.get());
    }

    @PutMapping("")
    public ResponseEntity<QuarterResponseDto> updateCurrentQuarter(@RequestBody CurrentQuarterRequestDto currentQuarterRequestDto) {
        return ResponseEntity.ok(currentQuarterService.update(currentQuarterRequestDto));
    }
}
