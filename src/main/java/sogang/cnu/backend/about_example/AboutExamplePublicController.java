package sogang.cnu.backend.about_example;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.about_example.dto.AboutExampleResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public/about-examples")
@RequiredArgsConstructor
public class AboutExamplePublicController {

    private final AboutExampleService aboutExampleService;

    @GetMapping
    public ResponseEntity<List<AboutExampleResponseDto>> getByCategory(
            @RequestParam AboutExampleCategory category
    ) {
        return ResponseEntity.ok(aboutExampleService.getByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AboutExampleResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(aboutExampleService.getById(id));
    }
}
