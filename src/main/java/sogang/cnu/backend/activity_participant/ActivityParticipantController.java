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
    public ResponseEntity<ActivityParticipantResponseDto> updateCompleted(@PathVariable Long id) {
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
    @GetMapping("/activities/{id}")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getByActivityId(
            @PathVariable("id") Long activityId) {
        return ResponseEntity.ok(activityParticipantService.getByActivityId(activityId));
    }

    @GetMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> getMyParticipant(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") Long activityId) {
        return ResponseEntity.ok(activityParticipantService.getByUserIdAndActivityId(user.getId(), activityId));
    }

    @PostMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> joinActivity(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") Long activityId) {
        return ResponseEntity.ok(activityParticipantService.createWithUserIdAndActivityId(user.getId(), activityId));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getByUserId(
            @PathVariable("id") Long userId) {
        return ResponseEntity.ok(activityParticipantService.getByUserId(userId));
    }




}
