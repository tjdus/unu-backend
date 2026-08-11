package sogang.cnu.backend.activity_opening_period;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_opening_period.dto.ActivityOpeningPeriodRequestDto;
import sogang.cnu.backend.activity_opening_period.dto.ActivityOpeningPeriodResponseDto;

@RestController
@RequestMapping("/api/manage/activity-opening-periods")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ActivityOpeningPeriodManagementController {
    private final ActivityOpeningPeriodService periodService;

    @GetMapping("/current")
    public ResponseEntity<ActivityOpeningPeriodResponseDto> getCurrent() {
        return ResponseEntity.ok(periodService.getCurrent());
    }

    @PutMapping("/current")
    public ResponseEntity<ActivityOpeningPeriodResponseDto> updateCurrent(
            @Valid @RequestBody ActivityOpeningPeriodRequestDto request
    ) {
        return ResponseEntity.ok(periodService.upsertCurrent(request));
    }
}
