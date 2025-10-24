package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @GetMapping("")
    public ResponseEntity<List<ActivityResponseDto>> getAll() {
        return ResponseEntity.ok(activityService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<ActivityResponseDto> create(@RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.create(activityRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> update(@PathVariable Long id, @RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.update(id, activityRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
