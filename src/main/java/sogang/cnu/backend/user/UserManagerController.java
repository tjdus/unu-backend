package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserRoleUpdateRequestDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/manager/users")
@RequiredArgsConstructor
public class UserManagerController {
    private final UserService userService;

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponseDto> setActiveStatus(@PathVariable UUID id, @RequestParam boolean active) {
        UserResponseDto response = userService.updateUserActiveStatus(id, active);
        return ResponseEntity.ok(response);
    }
}
