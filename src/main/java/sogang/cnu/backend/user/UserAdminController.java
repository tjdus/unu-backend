package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.auth.AuthService;
import sogang.cnu.backend.auth.dto.ResetPasswordResponseDto;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserRoleUpdateRequestDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserAdminController {
    private final UserService userService;
    private final AuthService authService;

    @PutMapping("/role")
    public ResponseEntity<UserResponseDto> changeUserRole(@RequestBody UserRoleUpdateRequestDto request) {
        UserResponseDto response = userService.changeUserRole(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(@PathVariable UUID userId) {
        ResetPasswordResponseDto response = authService.resetPassword(userId);
        return ResponseEntity.ok(response);
    }
}
