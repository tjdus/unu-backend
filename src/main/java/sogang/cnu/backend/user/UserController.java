package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserSummaryResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<UserResponseDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }


    @GetMapping("/studentId/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<UserResponseDto> getByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getByStudentId(studentId));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "is-active", required = false) Boolean isCurrentQuarterActive,
            @RequestParam(name = "joined-quarter", required = false) String joinedQuarter,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "student-id", required = false) String studentId
    ) {
        return ResponseEntity.ok(
                userService.search(role, isCurrentQuarterActive, joinedQuarter, name, studentId)
        );
    }

    @GetMapping("/search/summary")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','LECTURE_ROOM_MANAGER')")
    public ResponseEntity<List<UserSummaryResponseDto>> searchUserSummaries(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "student-id", required = false) String studentId
    ) {
        return ResponseEntity.ok(userService.searchSummaries(name, studentId));
    }

}
