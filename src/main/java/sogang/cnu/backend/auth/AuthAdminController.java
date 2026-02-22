package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.auth.dto.SignupTokenResponseDto;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthAdminController {
    private final AuthService authService;

    @PostMapping("/token")
    public ResponseEntity<SignupTokenResponseDto> generateSignupToken() {
        SignupTokenResponseDto response = authService.generateSignupToken();
        return ResponseEntity.ok(response);
    }
}