package sogang.cnu.backend.activity_session;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import sogang.cnu.backend.activity_session.dto.ActivitySessionRequestDto;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-sessions")
@RequiredArgsConstructor
public class ActivitySessionController {
    private final ActivitySessionService activitySessionService;

    @GetMapping("")
    public ResponseEntity<List<ActivitySessionResponseDto>> getAll() {
        return ResponseEntity.ok(activitySessionService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<ActivitySessionResponseDto> create(@RequestBody ActivitySessionRequestDto activitySessionRequestDto) {
        return ResponseEntity.ok(activitySessionService.create(activitySessionRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivitySessionResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(activitySessionService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivitySessionResponseDto> update(@PathVariable UUID id, @RequestBody ActivitySessionRequestDto activitySessionRequestDto) {
        return ResponseEntity.ok(activitySessionService.update(id, activitySessionRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        activitySessionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities/{id}")
    public ResponseEntity<List<ActivitySessionResponseDto>> getByActivityId(@PathVariable UUID id) {
        return ResponseEntity.ok(activitySessionService.getByActivityId(id));
    }

}
