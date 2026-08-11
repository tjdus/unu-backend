package sogang.cnu.backend.application;

import jakarta.validation.Valid;
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
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PostMapping("/lookup")
    public ResponseEntity<ApplicationLookupResponse> lookup(@Valid @RequestBody ApplicationLookupRequestDto query) {
        return ResponseEntity.ok(applicationService.lookup(query));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationRequestDto request,
            @RequestHeader(value = "X-Application-Token", required = false) String accessToken) {
        return ResponseEntity.ok(applicationService.update(id, request, accessToken));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<String> cancelByApplicant(
            @PathVariable UUID id,
            @RequestBody(required = false) PasswordRequestDto request,
            @RequestHeader(value = "X-Application-Token", required = false) String accessToken) {
        applicationService.cancelByApplicant(id, request != null ? request.getPassword() : null, accessToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<ApplicationVerificationResponse> verifyWithPassword(
            @PathVariable UUID id, @Valid @RequestBody PasswordRequestDto request) {
        return ResponseEntity.ok(applicationService.verify(id, request.getPassword()));
    }
}
