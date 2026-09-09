package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/hosted/me")
    public ResponseEntity<List<ActivityResponseDto>> getMyHostedActivities(
            @CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(activityService.getHostedByUserId(user.getId()));
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityResponseDto> create(@RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.create(activityRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> getById(@CurrentUser CustomUserDetails user, @PathVariable UUID id) {
        return ResponseEntity.ok(activityService.getById(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponseDto> update(@CurrentUser CustomUserDetails user, @PathVariable UUID id, @RequestBody ActivityRequestDto activityRequestDto) {
        return ResponseEntity.ok(activityService.update(user.getId(), id, activityRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@CurrentUser CustomUserDetails user, @PathVariable UUID id) {
        activityService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityResponseDto> updateStatus(@PathVariable UUID id, @RequestBody ActivityStatusRequestDto request) {
        return ResponseEntity.ok(activityService.updateStatus(id, request.getStatus()));
    }

                                                            @GetMapping("/search")
    public ResponseEntity<List<ActivityResponseDto>> search(@CurrentUser CustomUserDetails user,
                                                            @RequestParam(required = false) String title,
                                                            @RequestParam(required = false) String status,
                                                            @RequestParam(required = false) UUID activityTypeId,
                                                            @RequestParam(required = false) UUID quarterId,
                                                            @RequestParam(defaultValue = "false") boolean includeUnlisted) {
        ActivitySearchQuery query = ActivitySearchQuery.builder()
                .title(title)
                .status(status)
                .activityTypeId(activityTypeId)
                .quarterId(quarterId)
                .build();
        List<ActivityResponseDto> activities = activityService.search(query, includeUnlisted, user.getId());
        return ResponseEntity.ok(activities);
    }


}
