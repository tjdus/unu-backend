package sogang.cnu.backend.menu_notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import sogang.cnu.backend.menu_notification.dto.MenuNotificationSummaryDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.UUID;

@RestController
@RequestMapping("/api/menu-notifications")
@RequiredArgsConstructor
public class MenuNotificationController {
    private final MenuNotificationService menuNotificationService;

    @GetMapping("/unread-summary")
    public ResponseEntity<MenuNotificationSummaryDto> getSummary(
            @CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(menuNotificationService.getSummary(user.getId()));
    }

    @PostMapping("/activities/{activityId}/read")
    public ResponseEntity<Void> markActivityCardRead(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID activityId) {
        menuNotificationService.markActivityCardRead(user.getId(), activityId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/operation-recruitments/{recruitmentId}/read")
    public ResponseEntity<Void> markOperationRecruitmentCardRead(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID recruitmentId) {
        menuNotificationService.markOperationRecruitmentCardRead(user.getId(), recruitmentId);
        return ResponseEntity.noContent().build();
    }
}
