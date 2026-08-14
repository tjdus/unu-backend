package sogang.cnu.backend.recruitment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.application.ApplicationService;
import sogang.cnu.backend.application.dto.ApplicationResponse;
import sogang.cnu.backend.application.dto.OperationApplicationRequestDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentCompletionMessageResponseDto;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/operation-recruitments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class OperationRecruitmentController {
    private final RecruitmentService recruitmentService;
    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<RecruitmentResponseDto>> getAll() {
        return ResponseEntity.ok(recruitmentService.getOperationRecruitments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecruitmentResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(recruitmentService.getOperationRecruitment(id));
    }

    @GetMapping("/{id}/completion-message")
    public ResponseEntity<RecruitmentCompletionMessageResponseDto> getCompletionMessage(@PathVariable UUID id) {
        return ResponseEntity.ok(recruitmentService.getOperationCompletionMessage(id));
    }

    @PostMapping("/{id}/applications")
    public ResponseEntity<ApplicationResponse> apply(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody OperationApplicationRequestDto request) {
        return ResponseEntity.ok(applicationService.createOperation(id, user.getId(), request));
    }

    @GetMapping("/applications/me")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
            @CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(applicationService.getMyOperationApplications(user.getId()));
    }

    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationResponse> getMyApplication(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID applicationId) {
        return ResponseEntity.ok(applicationService.getMyOperationApplication(user.getId(), applicationId));
    }

    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<Void> cancelMyApplication(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID applicationId) {
        applicationService.cancelMyOperationApplication(user.getId(), applicationId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationResponse> updateMyApplication(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID applicationId,
            @Valid @RequestBody OperationApplicationRequestDto request) {
        return ResponseEntity.ok(
                applicationService.updateMyOperationApplication(user.getId(), applicationId, request));
    }
}
