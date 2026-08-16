package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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
    private static final String REFRESH_COOKIE = "refreshToken";

    private final AuthService authService;

    // 로컬 http 개발환경에서는 Secure 쿠키가 저장되지 않으므로 환경에 따라 분기한다.
    // production 컨테이너에서는 REFRESH_COOKIE_SECURE=true 로 주입한다.
    @Value("${auth.cookie.secure:false}")
    private boolean cookieSecure;

    // 리프레시 쿠키 만료를 토큰 만료와 동일하게 맞춰 슬라이딩 8시간을 유지한다.
    @Value("${jwt.refresh-token-expire-time:28800000}")
    private long refreshTokenExpireMs;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDto> signup(@RequestBody SignUpRequestDto request,
                                                   @RequestParam String token) {
        SignUpResponseDto signUpResponseDto = authService.signUp(request, token);
        return ResponseEntity.ok(signUpResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        AuthResult result = authService.login(loginRequestDto);
        return authResponse(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken) {
        AuthResult result = authService.refreshToken(refreshToken);
        return authResponse(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // refresh 토큰은 stateless라 서버 revoke는 없고, HttpOnly 쿠키만 만료시켜 제거한다.
        ResponseCookie expired = buildRefreshCookie("", 0);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .build();
    }

    // access 토큰과 사용자 정보는 JSON으로, refresh 토큰은 HttpOnly 쿠키로만 내려준다.
    private ResponseEntity<LoginResponseDto> authResponse(AuthResult result) {
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken(), refreshTokenExpireMs / 1000);
        LoginResponseDto body = LoginResponseDto.builder()
                .token(result.accessToken())
                .email(result.email())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    private ResponseCookie buildRefreshCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_COOKIE, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAgeSeconds)
                .build();
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
