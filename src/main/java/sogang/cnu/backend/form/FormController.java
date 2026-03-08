package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.form.dto.FormRequestDto;
import sogang.cnu.backend.form.dto.FormResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;
import java.util.List;
import java.util.UUID;

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
    public ResponseEntity<FormResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(formService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormResponseDto> update(@CurrentUser CustomUserDetails user, @PathVariable UUID id, @RequestBody FormRequestDto formRequestDto) {
        return ResponseEntity.ok(formService.update(user.getId(), id, formRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@CurrentUser CustomUserDetails user, @PathVariable UUID id) {
        formService.delete(user.getId(), id);
        return ResponseEntity.noContent().build();
    }


}
