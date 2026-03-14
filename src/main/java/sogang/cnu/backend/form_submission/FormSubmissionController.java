package sogang.cnu.backend.form_submission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<FormSubmissionResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(formSubmissionService.getById(id));
    }

    @GetMapping("/forms/{formId}")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        formSubmissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
