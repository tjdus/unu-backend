package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.application.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PostMapping("/public/applications/search")
    public ResponseEntity<ApplicationResponse> search(@RequestBody ApplicationSearchQuery query) {
        return ResponseEntity.ok(applicationService.search(query));
    }

    @PostMapping("/public/applications")
    public ResponseEntity<ApplicationResponse> create(@RequestBody ApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PutMapping("/public/applications/{id}")
    public ResponseEntity<ApplicationResponse> update(@PathVariable Long id, @RequestBody ApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.update(id, request));
    }

    @PatchMapping("/public/applications/{id}/cancel")
    public ResponseEntity<String> cancelWithPassword(@PathVariable Long id, @RequestBody PasswordRequestDto request) {
        applicationService.cancelWithPassword(id, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/public/applications/{id}/verify")
    public ResponseEntity<ApplicationResponse> getByIdWithPassword(@PathVariable Long id, @RequestBody PasswordRequestDto request) {
        return ResponseEntity.ok(applicationService.getByIdWithPassword(id, request.getPassword()));
    }

    @GetMapping("/admin/applications/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @GetMapping("/admin/recruitments/{recruitmentId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getByRecruitmentId(@PathVariable Long recruitmentId) {
        return ResponseEntity.ok(applicationService.getByRecruitmentId(recruitmentId));
    }

    @PatchMapping("/admin/applications/{id}/review")
    public ResponseEntity<ApplicationResponse> review(@PathVariable Long id, @RequestBody ApplicationReviewRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }

    @PostMapping("/admin/applications/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        applicationService.cancel(id);
        return ResponseEntity.noContent().build();
    }
}

