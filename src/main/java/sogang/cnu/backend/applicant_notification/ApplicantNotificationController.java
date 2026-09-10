package sogang.cnu.backend.applicant_notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.applicant_notification.dto.ApplicantNotificationSummaryDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

@RestController
@RequestMapping("/api/applicant-notifications")
@RequiredArgsConstructor
public class ApplicantNotificationController {

    private final ApplicantNotificationService applicantNotificationService;

    @GetMapping("/summary")
    public ResponseEntity<ApplicantNotificationSummaryDto> getSummary(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(applicantNotificationService.getSummary(user.getId()));
    }

    @PostMapping("/check")
    public ResponseEntity<ApplicantNotificationSummaryDto> check(@CurrentUser CustomUserDetails user) {
        return ResponseEntity.ok(applicantNotificationService.checkAndGetSummary(user.getId()));
    }
}
