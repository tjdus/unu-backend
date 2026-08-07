package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/quarters")
@RequiredArgsConstructor
public class QuarterController {
    private final QuarterService quarterService;

    @GetMapping("")
    public ResponseEntity<List<QuarterResponseDto>> getAll() {
        return ResponseEntity.ok(quarterService.getAll());
    }

    // 분기는 유저/포트폴리오/모집/활동 전반에서 참조되므로 관리자만 만들고 지울 수 있다.
    // (/admin/quarters 화면도 ADMIN 전용이라 그 기준에 맞춘다.)
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuarterResponseDto> create(@RequestBody QuarterRequestDto quarterRequestDto) {
        return ResponseEntity.ok(quarterService.create(quarterRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuarterResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(quarterService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuarterResponseDto> update(@PathVariable UUID id, @RequestBody QuarterRequestDto quarterRequestDto) {
        return ResponseEntity.ok(quarterService.update(id, quarterRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        quarterService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/search")
    public ResponseEntity<List<QuarterResponseDto>> searchQuarters(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "season") Season season
    ) {
        return ResponseEntity.ok(quarterService.searchByYearAndSeason(year, season));
    }

}
