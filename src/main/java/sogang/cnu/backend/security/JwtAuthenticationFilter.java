package sogang.cnu.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sogang.cnu.backend.user.MemberStatus;
import sogang.cnu.backend.user.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    private final List<String> whiteList = List.of(
            "/public/", "/api/auth/signup", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout"
    );

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if(isWhiteListed(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token) && jwtTokenProvider.isAccessToken(token)) {
            if (!isActiveUser(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("로그인이 필요한 요청입니다.");
                return;
            }

            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isWhiteListed(String uri) {
        return whiteList.stream().anyMatch(uri::startsWith);
    }

    private boolean isActiveUser(String token) {
        try {
            UUID userId = UUID.fromString(jwtTokenProvider.getIdFromToken(token));
            return userRepository.existsByIdAndMemberStatusNot(userId, MemberStatus.REMOVED);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
