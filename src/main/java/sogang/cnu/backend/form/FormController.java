package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.form.dto.FormRequestDto;
import sogang.cnu.backend.form.dto.FormResponseDto;
import java.util.List;

@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
public class FormController {
    private final FormService formService;

    @GetMapping("")
    public ResponseEntity<List<FormResponseDto>> getAll() {
        return ResponseEntity.ok(formService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<FormResponseDto> create(@RequestBody FormRequestDto formRequestDto) {
        return ResponseEntity.ok(formService.create(formRequestDto));
    }


    @GetMapping("/{id}")
    public ResponseEntity<FormResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormResponseDto> update(@PathVariable Long id, @RequestBody FormRequestDto formRequestDto) {
        return ResponseEntity.ok(formService.update(id, formRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        formService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
