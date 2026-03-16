package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.form.dto.FormTemplateRequestDto;
import sogang.cnu.backend.form.dto.FormTemplateResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/form-templates")
@RequiredArgsConstructor
public class FormTemplateController {
    private final FormTemplateService formTemplateService;

    @GetMapping("")
    public ResponseEntity<List<FormTemplateResponseDto>> getAll() {
        return ResponseEntity.ok(formTemplateService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<FormTemplateResponseDto> create(@RequestBody FormTemplateRequestDto formTemplateRequestDto) {
        return ResponseEntity.ok(formTemplateService.create(formTemplateRequestDto));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(formTemplateService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> update(@CurrentUser CustomUserDetails user, @PathVariable UUID id, @RequestBody FormTemplateRequestDto formTemplateRequestDto) {
        return ResponseEntity.ok(formTemplateService.update(user.getId(), id, formTemplateRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@CurrentUser CustomUserDetails user, @PathVariable UUID id) {
        formTemplateService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }


}
