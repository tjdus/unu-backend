package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.recruitment.dto.RecruitmentRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @GetMapping("")
    public ResponseEntity<List<RecruitmentResponseDto>> getAll() {
        return ResponseEntity.ok(recruitmentService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<RecruitmentResponseDto> create(@RequestBody RecruitmentRequestDto request) {
        return ResponseEntity.ok(recruitmentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(recruitmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecruitmentResponseDto> update(@CurrentUser CustomUserDetails user, @PathVariable UUID id, @RequestBody RecruitmentRequestDto request) {
        return ResponseEntity.ok(recruitmentService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@CurrentUser CustomUserDetails user, @PathVariable UUID id) {
        recruitmentService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }

}

