package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserResponseDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/studentId/{studentId}")
    public ResponseEntity<UserResponseDto> getByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getByStudentId(studentId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam(name = "role", required = false) String role,
            @RequestParam(name = "is-active", required = false) Boolean isActive,
            @RequestParam(name = "joined-quarter", required = false) String joinedQuarter,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "student-id", required = false) String studentId
    ) {
        return ResponseEntity.ok(
                userService.search(role, isActive, joinedQuarter, name, studentId)
        );
    }

}
