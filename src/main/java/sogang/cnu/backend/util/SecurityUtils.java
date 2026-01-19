package sogang.cnu.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import sogang.cnu.backend.security.CustomUserDetails;

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

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}

