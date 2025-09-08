package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.DTO.UserResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<List<UserResponseDTO>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/studentId/{studentId}")
    public ResponseEntity<UserResponseDTO> getByStudentId(@PathVariable String studentId) {
        return ResponseEntity.ok(userService.getByStudentId(studentId));
    }

}
