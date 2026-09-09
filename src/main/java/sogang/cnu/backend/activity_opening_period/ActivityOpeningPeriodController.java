package sogang.cnu.backend.activity_opening_period;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.activity_opening_period.dto.ActivityOpeningPeriodResponseDto;

@RestController
@RequestMapping("/api/activity-opening-periods")
@RequiredArgsConstructor
public class ActivityOpeningPeriodController {
    private final ActivityOpeningPeriodService periodService;

    @GetMapping("/current")
    public ResponseEntity<ActivityOpeningPeriodResponseDto> getCurrent() {
        return ResponseEntity.ok(periodService.getCurrent());
    }
}
