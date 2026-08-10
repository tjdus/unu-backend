package sogang.cnu.backend.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.notice.dto.NoticeListResponseDto;
import sogang.cnu.backend.notice.dto.NoticeRequestDto;
import sogang.cnu.backend.notice.dto.NoticeResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeListResponseDto> getAll() {
        return ResponseEntity.ok(noticeService.getAll(null));
    }

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeResponseDto> create(@RequestBody NoticeRequestDto dto) {
        return ResponseEntity.ok(noticeService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NoticeResponseDto> update(
            @PathVariable UUID id,
            @RequestBody NoticeRequestDto dto) {
        return ResponseEntity.ok(noticeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        noticeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
