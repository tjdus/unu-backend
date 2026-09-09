package sogang.cnu.backend.activity_type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_type.dto.ActivityTypeRequestDto;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-types")
@RequiredArgsConstructor
public class ActivityTypeController {
    private final ActivityTypeService activityTypeService;

    // 조회는 모든 활동 화면에서 쓰이므로 열어두고, 쓰기만 막는다.
    // 활동유형은 활동 전체가 참조하는 전역 마스터 데이터라 관리 페이지(/admin)와 동일하게 ADMIN 전용으로 둔다.
    @GetMapping("")
    public ResponseEntity<List<ActivityTypeResponseDto>> getAll() {
        return ResponseEntity.ok(activityTypeService.getAll());
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivityTypeResponseDto> create(@RequestBody ActivityTypeRequestDto activityTypeRequestDto) {
        return ResponseEntity.ok(activityTypeService.create(activityTypeRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(activityTypeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivityTypeResponseDto> update(@PathVariable UUID id, @RequestBody ActivityTypeRequestDto activityTypeRequestDto) {
        return ResponseEntity.ok(activityTypeService.update(id, activityTypeRequestDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        activityTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
