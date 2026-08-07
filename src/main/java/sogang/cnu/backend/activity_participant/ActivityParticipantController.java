package sogang.cnu.backend.activity_participant;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantRequestDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;


import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-participants")
@RequiredArgsConstructor
public class ActivityParticipantController {
    private final ActivityParticipantService activityParticipantService;

    // 활동 전체를 가로지르는 참가자 목록이므로 운영진만 조회할 수 있어야 한다.
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getAll() {
        return ResponseEntity.ok(activityParticipantService.getAll());
    }

    // 본인 참가는 /activities/{id}/me로 별도 제공되므로, 이 범용 생성은 운영진 전용이다.
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityParticipantResponseDto> create(@RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.create(activityParticipantRequestDto));
    }

    // 본인 것인지는 서비스에서 다시 확인한다.
    @GetMapping("/{id}")
    public ResponseEntity<ActivityParticipantResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(activityParticipantService.getById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityParticipantResponseDto> updateStatus(@PathVariable UUID id, @RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.updateStatus(id, activityParticipantRequestDto));
    }

    @PatchMapping("/{id}/completed")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityParticipantResponseDto> updateCompleted(@PathVariable UUID id) {
        return ResponseEntity.ok(activityParticipantService.updateCompleted(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ActivityParticipantResponseDto> update(@PathVariable UUID id, @RequestBody ActivityParticipantRequestDto activityParticipantRequestDto) {
        return ResponseEntity.ok(activityParticipantService.update(id, activityParticipantRequestDto));
    }

    // 본인 참가 취소(활동에서 나가기) 용도로 쓰이므로, 본인 것인지는 서비스에서 확인한다.
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        activityParticipantService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 활동 하나의 참가자 명단(전원)이므로 운영진 전용이다.
    @GetMapping("/activities/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getByActivityId(
            @PathVariable("id") UUID activityId) {
        return ResponseEntity.ok(activityParticipantService.getByActivityId(activityId));
    }

    @GetMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> getMyParticipant(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") UUID activityId) {
        return ResponseEntity.ok(activityParticipantService.getByUserIdAndActivityId(user.getId(), activityId));
    }

    @PostMapping("/activities/{id}/me")
    public ResponseEntity<ActivityParticipantResponseDto> joinActivity(
            @CurrentUser CustomUserDetails user,
            @PathVariable("id") UUID activityId) {
        return ResponseEntity.ok(activityParticipantService.createWithUserIdAndActivityId(user.getId(), activityId));
    }

    // 임의의 유저의 활동 이력 전체이므로 운영진 전용이다. 본인 이력은 /me로 조회한다.
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getByUserId(
            @PathVariable("id") UUID userId) {
        return ResponseEntity.ok(activityParticipantService.getByUserId(userId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ActivityParticipantResponseDto>> getMy(
          @CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(activityParticipantService.getByUserId(user.getId()));
    }




}
