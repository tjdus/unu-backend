package sogang.cnu.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.UUID;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static CustomUserDetails getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // TODO: Custom exception handling
        if (authentication == null ||
                !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new RuntimeException();
        }

        return (CustomUserDetails) authentication.getPrincipal();
    }

    public static UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static boolean isManagerOrAdmin() {
        return getCurrentUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_MANAGER"));
    }

    public static void requireOwner(String createdBy, String message) {
        if (!isOwner(createdBy)) {
            throw new ForbiddenException(message);
        }
    }

    public static void requireOwnerOrManager(String createdBy, String message) {
        if (!isOwner(createdBy) && !isManagerOrAdmin()) {
            throw new ForbiddenException(message);
        }
    }

    public static boolean isAdmin() {
        return getCurrentUser().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));
    }

    /** 게시물 수정/삭제 규칙: 작성자 본인이거나 ADMIN이어야 한다. MANAGER는 남의 글을 건드릴 수 없다. */
    public static void requireOwnerOrAdmin(String createdBy, String message) {
        if (!isOwner(createdBy) && !isAdmin()) {
            throw new ForbiddenException(message);
        }
    }

    private static boolean isOwner(String createdBy) {
        try {
            return createdBy != null && UUID.fromString(createdBy).equals(getCurrentUserId());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

