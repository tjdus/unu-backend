package sogang.cnu.backend.form_submission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.form_submission.dto.FormSubmissionRequestDto;
import sogang.cnu.backend.form_submission.dto.FormSubmissionResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/form-submissions")
@RequiredArgsConstructor
public class FormSubmissionController {

    private final FormSubmissionService formSubmissionService;

    // 본인 것인지는 서비스에서 다시 확인한다(getById/delete). 이 메서드 자체엔 역할 제한이 없다.
    @GetMapping("/{id}")
    public ResponseEntity<FormSubmissionResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(formSubmissionService.getById(id));
    }

    // 폼 하나에 달린 제출 전체를 반환하므로(다른 제출자의 답변 포함) 운영진만 접근 가능해야 한다.
    @GetMapping("/forms/{formId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<FormSubmissionResponseDto>> getByFormId(@PathVariable UUID formId) {
        return ResponseEntity.ok(formSubmissionService.getByFormId(formId));
    }

    @GetMapping("/me")
    public ResponseEntity<List<FormSubmissionResponseDto>> getMySubmissions(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(formSubmissionService.getByUserId(user.getId()));
    }

    @PostMapping("")
    public ResponseEntity<FormSubmissionResponseDto> create(
            @CurrentUser CustomUserDetails user,
            @RequestBody FormSubmissionRequestDto dto) {
        return ResponseEntity.ok(formSubmissionService.create(user.getId(), dto));
    }

    // 본인 제출 또는 운영진만 삭제할 수 있다 — 서비스에서 확인한다.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        formSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
