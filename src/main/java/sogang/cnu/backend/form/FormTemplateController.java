package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<FormTemplateResponseDto> create(@RequestBody FormTemplateRequestDto formTemplateRequestDto) {
        return ResponseEntity.ok(formTemplateService.create(formTemplateRequestDto));
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formTemplateService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormTemplateResponseDto> update(@PathVariable Long id, @RequestBody FormTemplateRequestDto formTemplateRequestDto) {
        return ResponseEntity.ok(formTemplateService.update(id, formTemplateRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        formTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
