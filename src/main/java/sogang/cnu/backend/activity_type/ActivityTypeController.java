package sogang.cnu.backend.activity_type;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_type.dto.ActivityTypeRequestDto;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/activity-types")
@RequiredArgsConstructor
public class ActivityTypeController {
    private final ActivityTypeService activityTypeService;

    @GetMapping("")
    public ResponseEntity<List<ActivityTypeResponseDto>> getAll() {
        return ResponseEntity.ok(activityTypeService.getAll());
    }

    @PostMapping("")
    public ResponseEntity<ActivityTypeResponseDto> create(@RequestBody ActivityTypeRequestDto activityTypeRequestDto) {
        return ResponseEntity.ok(activityTypeService.create(activityTypeRequestDto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityTypeResponseDto> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(activityTypeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityTypeResponseDto> update(@PathVariable Integer id, @RequestBody ActivityTypeRequestDto activityTypeRequestDto) {
        return ResponseEntity.ok(activityTypeService.update(id, activityTypeRequestDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        activityTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }


}
