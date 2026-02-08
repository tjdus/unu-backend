package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.form.dto.FormTemplateRequestDto;
import sogang.cnu.backend.form.dto.FormTemplateResponseDto;

import java.util.List;

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
    public ResponseEntity<FormTemplateResponseDto> create(@RequestBody FormTemplateRequestDto requestDto) {
        return ResponseEntity.ok(formTemplateService.create(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formTemplateService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> update(@PathVariable Long id, @RequestBody FormTemplateRequestDto requestDto) {
        return ResponseEntity.ok(formTemplateService.update(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

