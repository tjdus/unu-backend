package sogang.cnu.backend.activity_notice;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeRequestDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeResponseDto;
import sogang.cnu.backend.activity_notice.dto.ActivityNoticeUnreadSummaryDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

// 조회는 참여 확정자까지, 생성·수정·삭제는 운영진과 활동 담당자까지 허용된다.
// 역할만으로는 갈리지 않아서 서비스에서 활동 단위로 확인한다.
@RestController
@RequestMapping("/api/activity-notices")
@RequiredArgsConstructor
public class ActivityNoticeController {
    private final ActivityNoticeService activityNoticeService;

    @GetMapping
    public ResponseEntity<List<ActivityNoticeResponseDto>> getByActivityId(
            @CurrentUser CustomUserDetails user,
            @RequestParam UUID activityId
    ) {
        return ResponseEntity.ok(activityNoticeService.getByActivityId(user.getId(), activityId));
    }

    @GetMapping("/unread-summary")
    public ResponseEntity<ActivityNoticeUnreadSummaryDto> getUnreadSummary(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(activityNoticeService.getUnreadSummary(user.getId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id
    ) {
        activityNoticeService.markRead(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<ActivityNoticeResponseDto> create(
            @CurrentUser CustomUserDetails user,
            @Valid @RequestBody ActivityNoticeRequestDto request
    ) {
        return ResponseEntity.ok(activityNoticeService.create(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityNoticeResponseDto> update(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityNoticeRequestDto request
    ) {
        return ResponseEntity.ok(activityNoticeService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        activityNoticeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
