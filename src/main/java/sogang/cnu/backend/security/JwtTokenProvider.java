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
import java.util.stream.Stream;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

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
        String email = claims.getSubject();

        List<String> roles = getStringListClaim(claims, ROLES_CLAIM);

        List<String> permissions = getStringListClaim(claims, PERMISSIONS_CLAIM);

        List<SimpleGrantedAuthority> authorities = Stream.concat(
                roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role)),
                permissions.stream().map(SimpleGrantedAuthority::new)
        ).toList();

        User principal = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public String generateAccessToken(Long id, List<String> roles, List<String> permissions) {
        return generateToken(id, roles, permissions, accessTokenExpireTime, ACCESS_TOKEN_TYPE);
    }

    public String generateRefreshToken(Long id) {
        return generateToken(id, List.of(), List.of(), refreshTokenExpireTime, REFRESH_TOKEN_TYPE);
    }

    private String generateToken(Long id, List<String> roles, List<String> permissions, long expireTime, String tokenType) {
        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expireTime);

        Claims claims = Jwts.claims().setSubject(String.valueOf(id));
        claims.put(ROLES_CLAIM, roles);
        claims.put(PERMISSIONS_CLAIM, permissions);
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
