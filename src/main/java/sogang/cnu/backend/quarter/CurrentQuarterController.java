package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // 사이트 전역에서 참조하는 "현재 분기" 싱글톤이라 관리자만 바꿀 수 있어야 한다.
    @PutMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuarterResponseDto> updateCurrentQuarter(@RequestBody CurrentQuarterRequestDto currentQuarterRequestDto) {
        return ResponseEntity.ok(currentQuarterService.update(currentQuarterRequestDto));
    }
}
