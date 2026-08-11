package sogang.cnu.backend.lecture_material;

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
import sogang.cnu.backend.lecture_material.dto.LectureMaterialRequestDto;
import sogang.cnu.backend.lecture_material.dto.LectureMaterialResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lecture-materials")
@RequiredArgsConstructor
public class LectureMaterialController {
    private final LectureMaterialService lectureMaterialService;

    @GetMapping
    public ResponseEntity<List<LectureMaterialResponseDto>> getAll() {
        return ResponseEntity.ok(lectureMaterialService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LectureMaterialResponseDto> create(
            @Valid @RequestBody LectureMaterialRequestDto request
    ) {
        return ResponseEntity.ok(lectureMaterialService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LectureMaterialResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody LectureMaterialRequestDto request
    ) {
        return ResponseEntity.ok(lectureMaterialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lectureMaterialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
