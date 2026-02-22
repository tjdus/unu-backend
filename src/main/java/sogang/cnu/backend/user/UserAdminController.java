package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserRoleUpdateRequestDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;

    @PutMapping("/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@RequestBody UserRoleUpdateRequestDto request) {
        UserResponseDto response = userService.changeUserRole(request);
        return ResponseEntity.ok(response);
    }
}
