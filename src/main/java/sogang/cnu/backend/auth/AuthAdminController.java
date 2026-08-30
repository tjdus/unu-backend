package sogang.cnu.backend.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sogang.cnu.backend.auth.dto.SignupInvitationCreateRequestDto;
import sogang.cnu.backend.auth.dto.SignupInvitationExpirationRequestDto;
import sogang.cnu.backend.auth.dto.SignupInvitationMembersRequestDto;
import sogang.cnu.backend.auth.dto.SignupInvitationResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AuthAdminController {
    private final SignupInvitationService signupInvitationService;

    @GetMapping("/invitations")
    public ResponseEntity<List<SignupInvitationResponseDto>> getInvitations() {
        return ResponseEntity.ok(signupInvitationService.getAll());
    }

    @GetMapping("/invitations/{invitationId}")
    public ResponseEntity<SignupInvitationResponseDto> getInvitation(
            @PathVariable UUID invitationId
    ) {
        return ResponseEntity.ok(signupInvitationService.getById(invitationId));
    }

    @PostMapping("/invitations")
    public ResponseEntity<SignupInvitationResponseDto> createInvitation(
            @Valid @RequestBody SignupInvitationCreateRequestDto request
    ) {
        return ResponseEntity.ok(signupInvitationService.create(request));
    }

    @PostMapping("/invitations/{invitationId}/members")
    public ResponseEntity<SignupInvitationResponseDto> addMembers(
            @PathVariable UUID invitationId,
            @Valid @RequestBody SignupInvitationMembersRequestDto request
    ) {
        return ResponseEntity.ok(signupInvitationService.addMembers(invitationId, request.getStudentIds()));
    }

    @DeleteMapping("/invitations/{invitationId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID invitationId,
            @PathVariable UUID memberId
    ) {
        signupInvitationService.removeMember(invitationId, memberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/invitations/{invitationId}/expiration")
    public ResponseEntity<SignupInvitationResponseDto> updateExpiration(
            @PathVariable UUID invitationId,
            @Valid @RequestBody SignupInvitationExpirationRequestDto request
    ) {
        return ResponseEntity.ok(
                signupInvitationService.updateExpiration(invitationId, request.getExpiresAt())
        );
    }

    @PostMapping("/invitations/{invitationId}/revoke")
    public ResponseEntity<SignupInvitationResponseDto> revokeInvitation(
            @PathVariable UUID invitationId
    ) {
        return ResponseEntity.ok(signupInvitationService.revoke(invitationId));
    }
}
