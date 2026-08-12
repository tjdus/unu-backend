package sogang.cnu.backend.lecture_material;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/activity/{activityId}")
    public ResponseEntity<List<LectureMaterialResponseDto>> getByActivityId(
            @PathVariable UUID activityId
    ) {
        return ResponseEntity.ok(lectureMaterialService.getByActivityId(activityId));
    }

    // 운영진 외에 활동 담당자(개설자)도 자기 활동의 자료를 관리할 수 있어야 해서,
    // 역할만으로 거르지 않고 서비스에서 담당자 여부까지 확인한다.
    @PostMapping
    public ResponseEntity<LectureMaterialResponseDto> create(
            @Valid @RequestBody LectureMaterialRequestDto request
    ) {
        return ResponseEntity.ok(lectureMaterialService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LectureMaterialResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody LectureMaterialRequestDto request
    ) {
        return ResponseEntity.ok(lectureMaterialService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lectureMaterialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
