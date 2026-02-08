package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.application.dto.ApplicationCreateRequest;
import sogang.cnu.backend.application.dto.ApplicationResponse;
import sogang.cnu.backend.application.dto.ApplicationReviewRequest;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping("/recruitments/{recruitmentId}/applications")
    public ResponseEntity<ApplicationResponse> create(@PathVariable Long recruitmentId, @RequestBody ApplicationCreateRequest request) {
        request.setRecruitmentId(recruitmentId);
        return ResponseEntity.ok(applicationService.create(request));
    }

    @GetMapping("/recruitments/{recruitmentId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getByRecruitmentId(@PathVariable Long recruitmentId) {
        return ResponseEntity.ok(applicationService.getByRecruitmentId(recruitmentId));
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @PatchMapping("/applications/{id}/review")
    public ResponseEntity<ApplicationResponse> review(@PathVariable Long id, @RequestBody ApplicationReviewRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/applications/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        applicationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}

