package sogang.cnu.backend.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.notice.dto.NoticeListResponseDto;
import sogang.cnu.backend.notice.dto.NoticeRequestDto;
import sogang.cnu.backend.notice.dto.NoticeResponseDto;
import sogang.cnu.backend.notice.dto.NoticeUnreadSummaryDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/unread-summary")
    public ResponseEntity<NoticeUnreadSummaryDto> getUnreadSummary(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(noticeService.getUnreadSummary(user.getId()));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id
    ) {
        noticeService.markRead(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeListResponseDto> getAll() {
        return ResponseEntity.ok(noticeService.getAll(null));
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeResponseDto> create(
            @CurrentUser CustomUserDetails user,
            @RequestBody NoticeRequestDto dto
    ) {
        return ResponseEntity.ok(noticeService.create(user.getId(), dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeResponseDto> update(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @RequestBody NoticeRequestDto dto) {
        return ResponseEntity.ok(noticeService.update(user.getId(), id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
