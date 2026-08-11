package sogang.cnu.backend.activity_opening_request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningRequestDto;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningRequestResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;
import sogang.cnu.backend.user.UserService;
import sogang.cnu.backend.user.dto.UserSummaryResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activity-opening-requests")
@RequiredArgsConstructor
public class ActivityOpeningRequestController {
    private final ActivityOpeningRequestService requestService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ActivityOpeningRequestResponseDto> createDraft(
            @CurrentUser CustomUserDetails user,
            @Valid @RequestBody ActivityOpeningRequestDto request
    ) {
        return ResponseEntity.ok(requestService.createDraft(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityOpeningRequestResponseDto> update(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityOpeningRequestDto request
    ) {
        return ResponseEntity.ok(requestService.update(user.getId(), id, request));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ActivityOpeningRequestResponseDto> submit(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(requestService.submit(user.getId(), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ActivityOpeningRequestResponseDto> cancel(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(requestService.cancel(user.getId(), id));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ActivityOpeningRequestResponseDto>> getMine(
            @CurrentUser CustomUserDetails user
    ) {
        return ResponseEntity.ok(requestService.getMine(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityOpeningRequestResponseDto> get(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(requestService.get(user.getId(), id));
    }

    @GetMapping("/members/search")
    public ResponseEntity<List<UserSummaryResponseDto>> searchMembers(@RequestParam String query) {
        return ResponseEntity.ok(userService.searchActiveSummaries(query));
    }
}
