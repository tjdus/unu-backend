package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.application.dto.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/applications")
@RequiredArgsConstructor
public class ApplicationPublicController {
    private final ApplicationService applicationService;

    @PostMapping("")
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PostMapping("/lookup")
    public ResponseEntity<ApplicationResponse> lookup(@RequestBody ApplicationLookupRequestDto query) {
        return ResponseEntity.ok(applicationService.lookup(query));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update(@PathVariable UUID id, @RequestBody ApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.update(id, request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelWithPassword(@PathVariable UUID id, @RequestBody PasswordRequestDto request) {
        applicationService.cancelWithPassword(id, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<ApplicationResponse> getByIdWithPassword(@PathVariable UUID id, @RequestBody PasswordRequestDto request) {
        return ResponseEntity.ok(applicationService.getByIdWithPassword(id, request.getPassword()));
    }
}

