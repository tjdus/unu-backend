package sogang.cnu.backend.auth.dto;

/**
 * 로그인·토큰 재발급 시 서비스가 컨트롤러로 전달하는 내부 결과.
 * refreshToken은 HttpOnly 쿠키로만 내려가므로 JSON 응답(LoginResponseDto)에는 포함되지 않는다.
 */
public record AuthResult(
        String accessToken,
        String refreshToken,
        String email
) {
}
