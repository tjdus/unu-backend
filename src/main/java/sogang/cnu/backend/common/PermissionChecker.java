package sogang.cnu.backend.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.user_role.UserRoleRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PermissionChecker {
    private final UserRoleRepository userRoleRepository;

    public void checkManagerOrAdmin(UUID userId) {
        boolean hasPrivilegedRole = userRoleRepository.findByUserId(userId).stream()
                .map(ur -> ur.getRole().getName())
                .anyMatch(name -> name.equals("MANAGER") || name.equals("ADMIN"));

        if (!hasPrivilegedRole) {
            throw new ForbiddenException("You do not have permission to perform this action.");
        }
    }
}
