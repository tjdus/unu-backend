package sogang.cnu.backend.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.application.dto.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;

    @PreAuthorize("hasAnyRole('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @PreAuthorize("hasAnyRole('MANAGER')")
    @GetMapping("/{recruitmentId}/applications")
    public ResponseEntity<List<ApplicationResponse>> getByRecruitmentId(@PathVariable Long recruitmentId) {
        return ResponseEntity.ok(applicationService.getByRecruitmentId(recruitmentId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/{id}/review")
    public ResponseEntity<ApplicationResponse> review(@PathVariable Long id, @RequestBody ApplicationReviewRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request.getStatus()));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        applicationService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

