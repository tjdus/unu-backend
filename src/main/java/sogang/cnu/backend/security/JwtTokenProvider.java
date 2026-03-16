package sogang.cnu.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String SIGNUP_TOKEN_TYPE = "signup";
    private static final long SIGNUP_TOKEN_EXPIRE_TIME = 86400000L; // 24 hours

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expire-time:3600000}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expire-time:604800000}")
    private long refreshTokenExpireTime;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        log.info("JWT TokenProvider initialized");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        UUID userId = UUID.fromString(claims.getSubject());
        String email = claims.getSubject();

        List<String> roles = getStringListClaim(claims, ROLES_CLAIM);

        List<SimpleGrantedAuthority> authorities =
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();

        CustomUserDetails principal =
                new CustomUserDetails(userId, email, authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String generateAccessToken(UUID id, List<String> roles) {
        return generateToken(id, roles, accessTokenExpireTime, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(UUID id) {
        return generateToken(id, List.of(), refreshTokenExpireTime, REFRESH_TOKEN_TYPE);
    }

    public String generateSignupToken() {
        long now = System.currentTimeMillis();
        Claims claims = Jwts.claims().setSubject("signup-invite");
        claims.put(TOKEN_TYPE_CLAIM, SIGNUP_TOKEN_TYPE);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + SIGNUP_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isSignupToken(String token) {
        Claims claims = getClaims(token);
        return SIGNUP_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    private String generateToken(UUID id, List<String> roles, long expireTime, String tokenType) {
        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expireTime);

        Claims claims = Jwts.claims().setSubject(String.valueOf(id));
        claims.put(ROLES_CLAIM, roles);
        claims.put(TOKEN_TYPE_CLAIM, tokenType);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getIdFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public boolean isRefreshToken(String token) {
        Claims claims = getClaims(token);
        return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isTokenExpired(String token) {
        try {
            return getClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private static List<String> getStringListClaim(Claims claims, String key) {
        Object value = claims.get(key);
        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                    .filter(item -> item instanceof String)
                    .map(item -> (String) item)
                    .toList();
        }
        return List.of();
    }
}
