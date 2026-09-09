package sogang.cnu.backend.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.notice.dto.NoticeListResponseDto;
import sogang.cnu.backend.notice.dto.NoticeResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/notices")
@RequiredArgsConstructor
public class NoticePublicController {

    private final NoticeService noticeService;

    @GetMapping("")
    public ResponseEntity<NoticeListResponseDto> getAll(
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(noticeService.getAll(limit));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(noticeService.getById(id));
    }
}
