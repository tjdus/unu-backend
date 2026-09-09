package sogang.cnu.backend.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.auth.AuthService;
import sogang.cnu.backend.auth.dto.ResetPasswordResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;
import sogang.cnu.backend.user.dto.UserRemovalRequestDto;
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

    @PostMapping("/calculate-active")
    public ResponseEntity<Void> calculateCurrentQuarterActive() {
        userService.calculateAndUpdateCurrentQuarterActive();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeUsers(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody UserRemovalRequestDto request
    ) {
        userService.removeUsers(currentUser.getId(), request.getUserIds());
        return ResponseEntity.noContent().build();
    }
}
