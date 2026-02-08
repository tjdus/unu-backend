package sogang.cnu.backend.recruitment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.recruitment.dto.RecruitmentRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

import java.util.List;

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
    public ResponseEntity<RecruitmentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recruitmentService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecruitmentResponseDto> update(@PathVariable Long id, @RequestBody RecruitmentRequestDto request) {
        return ResponseEntity.ok(recruitmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recruitmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

