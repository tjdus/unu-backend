package sogang.cnu.backend.about_example;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.about_example.dto.AboutExampleReorderDto;
import sogang.cnu.backend.about_example.dto.AboutExampleRequestDto;
import sogang.cnu.backend.about_example.dto.AboutExampleResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/about-examples")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class AboutExampleController {

    private final AboutExampleService aboutExampleService;

    @PostMapping
    public ResponseEntity<AboutExampleResponseDto> create(@RequestBody AboutExampleRequestDto dto) {
        return ResponseEntity.ok(aboutExampleService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AboutExampleResponseDto> update(
            @PathVariable UUID id,
            @RequestBody AboutExampleRequestDto dto
    ) {
        return ResponseEntity.ok(aboutExampleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        aboutExampleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@Valid @RequestBody AboutExampleReorderDto request) {
        aboutExampleService.reorder(request.getOrderedIds());
        return ResponseEntity.noContent().build();
    }
}
