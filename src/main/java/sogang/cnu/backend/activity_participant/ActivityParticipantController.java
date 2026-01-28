package sogang.cnu.backend.activity_participant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantRequestDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;


import java.util.List;

@RestController
@RequestMapping("/api/activity-participants")
@RequiredArgsConstructor
public class ActivityParticipantController {
    private final ActivityParticipantService activityParticipantService;

    @GetMapping("")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getAll() {
        return ResponseEntity.ok(activityParticipantService.getAll());
    }

    @GetMapping("/test")
    public ResponseEntity<String> test(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(user.getEmail() + " " + user.getId());
    }

    @PostMapping("")
    public ResponseEntity<ActivityParticipantResponseDto> create(@RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.create(activityParticipantRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityParticipantResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(activityParticipantService.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ActivityParticipantResponseDto> updateStatus(@PathVariable Long id, @RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.updateStatus(id, activityParticipantRequestDto));
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<ActivityParticipantResponseDto> updateCompleted(@PathVariable Long id, @RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.updateCompleted(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityParticipantResponseDto> update(@PathVariable Long id, @RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.update(id, activityParticipantRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        activityParticipantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> getMyParticipantByActivityId(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") Long activityId) {
        return ResponseEntity.ok(activityParticipantService.getMyParticipantByActivityId(user.getId(), activityId));
    }

    @PostMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> createMyParticipantByActivityId(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") Long activityId) {
        return ResponseEntity.ok(activityParticipantService.createMyParticipantByActivityId(user.getId(), activityId));
    }




}
