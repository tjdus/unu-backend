package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/public/users")
@RequiredArgsConstructor
public class UserPublicController {
    private final UserService userService;

    @GetMapping("/studentId/{studentId}")
    public ResponseEntity<UserResponseDto> getByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getByStudentId(studentId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "student-id", required = false) String studentId
    ) {
        return ResponseEntity.ok(
                userService.search(null, null, null, name, studentId)
        );
    }

}
