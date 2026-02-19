package sogang.cnu.backend.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.auth.dto.*;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;
import sogang.cnu.backend.user.dto.UserResponseDto;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@RequestBody SignUpRequestDto request) {
        SignUpResponseDto signUpResponseDto = authService.signUp(request);
        return ResponseEntity.ok(signUpResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = authService.login(loginRequestDto);
        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok("로그아웃 하셨습니다.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshTokenRequestDto request) {
        LoginResponseDto response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDto> getCurrentUserInfo(@CurrentUser CustomUserDetails user) {
        System.out.println(user.getId());
        UserInfoResponseDto response = authService.getUserInfo(user.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateUserInfo(@CurrentUser CustomUserDetails user, @RequestBody UserInfoRequestDto request) {
        UserResponseDto response = authService.update(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<String> changePassword(@CurrentUser CustomUserDetails user, @RequestBody PasswordUpdateRequestDto request) {
        authService.updatePassword(user.getId(), request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }
}
