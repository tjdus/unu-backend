package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/quarters")
@RequiredArgsConstructor
public class QuarterPublicController {
    private final QuarterService quarterService;

    @GetMapping("")
    public ResponseEntity<List<QuarterResponseDto>> getAll() {
        return ResponseEntity.ok(quarterService.getAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<QuarterResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(quarterService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuarterResponseDto>> searchQuarters(
            @RequestParam(name = "year") int year,
            @RequestParam(name = "season") Season season
    ) {
        return ResponseEntity.ok(quarterService.searchByYearAndSeason(year, season));
    }

}
