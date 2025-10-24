package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/quarters")
@RequiredArgsConstructor
public class QuarterController {
    private final QuarterService quarterService;

    @GetMapping("")
    public ResponseEntity<List<QuarterResponseDto>> getAll() {
        return ResponseEntity.ok(quarterService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<QuarterResponseDto> create(@RequestBody QuarterRequestDto quarterRequestDto) {
        return ResponseEntity.ok(quarterService.create(quarterRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuarterResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(quarterService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuarterResponseDto> update(@PathVariable Long id, @RequestBody QuarterRequestDto quarterRequestDto) {
        return ResponseEntity.ok(quarterService.update(id, quarterRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
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
