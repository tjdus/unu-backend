package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;
import sogang.cnu.backend.activity.dto.ActivityStatusRequestDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/me")
    public ResponseEntity<ActivityResponseDto> createForMe(@CurrentUser CustomUserDetails user, @RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.createWithAssignee(user.getId(), activityRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> update(@PathVariable UUID id, @RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.update(id, activityRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        activityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ActivityResponseDto> updateStatus(@PathVariable UUID id, @RequestBody ActivityStatusRequestDto request) {
        return ResponseEntity.ok(activityService.updateStatus(id, request.getStatus()));
    }

                                                            @GetMapping("/search")
    public ResponseEntity<List<ActivityResponseDto>> search(@RequestParam(required = false) String title,
                                                            @RequestParam(required = false) String status,
                                                            @RequestParam(required = false) UUID activityTypeId,
                                                            @RequestParam(required = false) UUID quarterId) {
        ActivitySearchQuery query = ActivitySearchQuery.builder()
                .title(title)
                .status(status)
                .activityTypeId(activityTypeId)
                .quarterId(quarterId)
                .build();
        List<ActivityResponseDto> activities = activityService.search(query);
        return ResponseEntity.ok(activities);
    }


}
