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
    public ResponseEntity<FormResponseDto> create(@RequestBody FormRequestDto requestDto) {
        return ResponseEntity.ok(formService.create(requestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(formService.getById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FormResponseDto> update(@PathVariable Long id, @RequestBody FormRequestDto requestDto) {
        return ResponseEntity.ok(formService.update(id, requestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        formService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

