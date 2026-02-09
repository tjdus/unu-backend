package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.recruitment.dto.RecruitmentRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {
    private final RecruitmentService recruitmentService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("")
    public ResponseEntity<List<RecruitmentResponseDto>> getAll() {
        return ResponseEntity.ok(recruitmentService.getAll());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("")
    public ResponseEntity<RecruitmentResponseDto> create(@RequestBody RecruitmentRequestDto request) {
        return ResponseEntity.ok(recruitmentService.create(request));
    }

    @GetMapping("/public/recruitments/{id}")
    public ResponseEntity<RecruitmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentService.getById(id));
    }

    @PutMapping("/recruitments/{id}")
    public ResponseEntity<RecruitmentResponseDto> update(@PathVariable Long id, @RequestBody RecruitmentRequestDto request) {
        return ResponseEntity.ok(recruitmentService.update(id, request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recruitmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

