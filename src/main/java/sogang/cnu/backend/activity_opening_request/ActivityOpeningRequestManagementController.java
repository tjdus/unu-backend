package sogang.cnu.backend.activity_opening_request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningApprovalRequestDto;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningRequestResponseDto;
import sogang.cnu.backend.activity_opening_request.dto.ActivityOpeningReviewRequestDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/manage/activity-opening-requests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ActivityOpeningRequestManagementController {
    private final ActivityOpeningRequestService requestService;

    @GetMapping
    public ResponseEntity<List<ActivityOpeningRequestResponseDto>> getAll() {
        return ResponseEntity.ok(requestService.getAllForManagement());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityOpeningRequestResponseDto> get(@PathVariable UUID id) {
        return ResponseEntity.ok(requestService.getForManagement(id));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ActivityOpeningRequestResponseDto> review(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody ActivityOpeningReviewRequestDto request
    ) {
        try {
            ActivityOpeningRequestStatus status = ActivityOpeningRequestStatus.valueOf(request.getStatus());
            return ResponseEntity.ok(requestService.review(user.getId(), id, status, request.getComment()));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("올바른 검토 상태를 선택해주세요.");
        }
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ActivityOpeningRequestResponseDto> approve(
            @CurrentUser CustomUserDetails user,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) ActivityOpeningApprovalRequestDto request
    ) {
        String comment = request == null ? null : request.getComment();
        Integer depositAmount = request == null ? null : request.getDepositAmount();

        return ResponseEntity.ok(
                requestService.approve(
                        user.getId(),
                        id,
                        comment,
                        depositAmount
                )
        );
    }
}
