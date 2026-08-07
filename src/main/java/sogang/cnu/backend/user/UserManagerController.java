package sogang.cnu.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserRoleUpdateRequestDto;

import java.util.UUID;

// "/api/manager/**" 경로는 이름과 달리 SecurityConfig에서 role로 보호되지 않으므로
// (오직 "/api/admin/**"만 경로 단위로 보호됨) 메서드에 직접 @PreAuthorize를 건다.
@RestController
@RequestMapping("/api/manager/users")
@RequiredArgsConstructor
public class UserManagerController {
    private final UserService userService;

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<UserResponseDto> setActiveStatus(@PathVariable UUID id, @RequestParam boolean active) {
        UserResponseDto response = userService.updateUserActiveStatus(id, active);
        return ResponseEntity.ok(response);
    }
}
