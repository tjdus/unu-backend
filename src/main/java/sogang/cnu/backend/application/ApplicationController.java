package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.application.dto.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @GetMapping("/recruitments/{recruitmentId}")
    public ResponseEntity<List<ApplicationResponse>> getByRecruitmentId(@PathVariable UUID recruitmentId) {
        return ResponseEntity.ok(applicationService.getByRecruitmentId(recruitmentId));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApplicationResponse> review(@PathVariable UUID id, @RequestBody ApplicationReviewRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id) {
        applicationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id){
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

