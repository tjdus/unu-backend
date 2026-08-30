package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.application.Application;
import sogang.cnu.backend.auth.dto.SignupEligibilityResponseDto;
import sogang.cnu.backend.auth.dto.SignupInvitationCreateRequestDto;
import sogang.cnu.backend.auth.dto.SignupInvitationMemberResponseDto;
import sogang.cnu.backend.auth.dto.SignupInvitationResponseDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.common.exception.UnauthorizedException;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.recruitment.Recruitment;
import sogang.cnu.backend.recruitment.RecruitmentType;
import sogang.cnu.backend.security.JwtTokenProvider;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignupInvitationService {
    private static final int MAX_MEMBERS = 500;

    private final SignupInvitationRepository invitationRepository;
    private final SignupInvitationMemberRepository memberRepository;
    private final QuarterRepository quarterRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupInvitationResponseDto create(SignupInvitationCreateRequestDto dto) {
        Set<String> studentIds = normalizeStudentIds(dto.getStudentIds());
        validateRegisteredStudents(studentIds);

        Quarter quarter = quarterRepository.findById(dto.getJoinedQuarterId())
                .orElseThrow(() -> new BadRequestException("존재하지 않는 가입 분기입니다."));
        SignupInvitation invitation = invitationRepository.saveAndFlush(
                SignupInvitation.create(dto.getName().trim(), quarter, dto.getExpiresAt())
        );

        List<SignupInvitationMember> members = studentIds.stream()
                .map(studentId -> SignupInvitationMember.create(invitation, studentId))
                .toList();
        memberRepository.saveAll(members);
        memberRepository.flush();
        return toResponse(invitation, members);
    }

    @Transactional(readOnly = true)
    public List<SignupInvitationResponseDto> getAll() {
        return invitationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(invitation -> toResponse(invitation, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SignupInvitationResponseDto getById(UUID invitationId) {
        SignupInvitation invitation = findInvitation(invitationId);
        return toResponse(
                invitation,
                memberRepository.findByInvitationIdOrderByStudentIdAsc(invitationId)
        );
    }

    @Transactional
    public SignupInvitationResponseDto addMembers(UUID invitationId, List<String> requestedStudentIds) {
        SignupInvitation invitation = findInvitationForUpdate(invitationId);
        validateActive(invitation);

        Set<String> studentIds = normalizeStudentIds(requestedStudentIds);
        validateRegisteredStudents(studentIds);
        long currentCount = memberRepository.countByInvitationId(invitationId);
        List<String> newStudentIds = studentIds.stream()
                .filter(studentId -> !memberRepository.existsByInvitationIdAndStudentId(invitationId, studentId))
                .toList();
        if (currentCount + newStudentIds.size() > MAX_MEMBERS) {
            throw new BadRequestException("한 초대에는 최대 500명까지 등록할 수 있습니다.");
        }

        memberRepository.saveAll(
                newStudentIds.stream()
                        .map(studentId -> SignupInvitationMember.create(invitation, studentId))
                        .toList()
        );
        memberRepository.flush();
        return getById(invitationId);
    }

    @Transactional
    public void removeMember(UUID invitationId, UUID memberId) {
        findInvitationForUpdate(invitationId);
        SignupInvitationMember member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("초대 학번을 찾을 수 없습니다."));
        if (!member.getInvitation().getId().equals(invitationId)) {
            throw new BadRequestException("해당 초대에 포함된 학번이 아닙니다.");
        }
        if (member.isUsed()) {
            throw new BadRequestException("가입을 완료한 학번은 제거할 수 없습니다.");
        }
        memberRepository.delete(member);
    }

    @Transactional
    public void addPassedApplication(Application application) {
        Recruitment recruitment = application.getRecruitment();
        if (recruitment.getType() != RecruitmentType.NEW_MEMBER) {
            return;
        }
        if (recruitment.getQuarter() == null) {
            throw new BadRequestException("회원가입 초대를 만들려면 모집의 가입 분기가 필요합니다.");
        }
        SignupInvitation invitation = invitationRepository
                .findBySourceRecruitmentIdForUpdate(recruitment.getId())
                .orElseGet(() -> invitationRepository.saveAndFlush(
                        SignupInvitation.createForRecruitment(
                                recruitment.getTitle() + " 합격자",
                                recruitment.getId(),
                                recruitment.getQuarter(),
                                Instant.now().plus(30, ChronoUnit.DAYS)
                        )
                ));
        if (invitation.isRevoked()) {
            throw new BadRequestException("종료된 회원가입 초대가 연결되어 있습니다. 회원가입 관리에서 새 초대를 만들어주세요.");
        }
        if (invitation.isExpired(Instant.now())) {
            invitation.updateExpiration(Instant.now().plus(30, ChronoUnit.DAYS));
        }
        if (userRepository.existsByStudentId(application.getStudentId())) {
            return;
        }

        SignupInvitationMember member = memberRepository
                .findByInvitationIdAndStudentId(invitation.getId(), application.getStudentId())
                .orElse(null);
        if (member != null) {
            if (!member.isUsed()) {
                member.updateProfile(application);
            }
            return;
        }
        if (memberRepository.countByInvitationId(invitation.getId()) >= MAX_MEMBERS) {
            throw new BadRequestException("한 초대에는 최대 500명까지 등록할 수 있습니다.");
        }
        memberRepository.save(SignupInvitationMember.createFromApplication(invitation, application));
    }

    @Transactional
    public void removePassedApplication(Application application) {
        Recruitment recruitment = application.getRecruitment();
        if (recruitment.getType() != RecruitmentType.NEW_MEMBER) {
            return;
        }
        invitationRepository.findBySourceRecruitmentIdForUpdate(recruitment.getId())
                .flatMap(invitation -> memberRepository.findForSignup(
                        invitation.getId(), application.getStudentId()))
                .filter(member -> !member.isUsed())
                .ifPresent(memberRepository::delete);
    }

    @Transactional
    public SignupInvitationResponseDto updateExpiration(UUID invitationId, Instant expiresAt) {
        SignupInvitation invitation = findInvitationForUpdate(invitationId);
        if (invitation.isRevoked()) {
            throw new BadRequestException("종료된 초대의 만료 시간은 변경할 수 없습니다.");
        }
        invitation.updateExpiration(expiresAt);
        return toResponse(
                invitation,
                memberRepository.findByInvitationIdOrderByStudentIdAsc(invitationId)
        );
    }

    @Transactional
    public SignupInvitationResponseDto revoke(UUID invitationId) {
        SignupInvitation invitation = findInvitationForUpdate(invitationId);
        if (!invitation.isRevoked()) {
            invitation.revoke(Instant.now());
        }
        return toResponse(
                invitation,
                memberRepository.findByInvitationIdOrderByStudentIdAsc(invitationId)
        );
    }

    @Transactional(readOnly = true)
    public SignupEligibilityResponseDto verifyEligibility(String token, String studentId) {
        SignupInvitation invitation = findInvitationFromToken(token, false);
        String normalizedStudentId = normalizeStudentId(studentId);
        SignupInvitationMember member = memberRepository
                .findByInvitationIdAndStudentId(invitation.getId(), normalizedStudentId)
                .orElseThrow(() -> new UnauthorizedException("가입 대상 학번이 아닙니다."));
        validateUnusedMember(member);
        if (userRepository.existsByStudentId(normalizedStudentId)) {
            throw new BadRequestException("이미 가입된 학번입니다.");
        }
        return SignupEligibilityResponseDto.builder()
                .invitationName(invitation.getName())
                .studentId(normalizedStudentId)
                .joinedQuarterId(invitation.getJoinedQuarter().getId())
                .joinedQuarterName(invitation.getJoinedQuarter().getName())
                .name(member.getName())
                .major(member.getMajor())
                .subMajor(member.getSubMajor())
                .email(member.getEmail())
                .githubId(member.getGithubId())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }

    public SignupInvitationMember findEligibleMemberForSignup(String token, String studentId) {
        SignupInvitation invitation = findInvitationFromToken(token, true);
        SignupInvitationMember member = memberRepository
                .findForSignup(invitation.getId(), normalizeStudentId(studentId))
                .orElseThrow(() -> new UnauthorizedException("가입 대상 학번이 아닙니다."));
        validateUnusedMember(member);
        return member;
    }

    private SignupInvitation findInvitationFromToken(String token, boolean forUpdate) {
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isSignupToken(token)) {
            throw new UnauthorizedException("유효하지 않거나 만료된 회원가입 링크입니다.");
        }

        UUID invitationId;
        try {
            invitationId = UUID.fromString(jwtTokenProvider.getIdFromToken(token));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("유효하지 않거나 만료된 회원가입 링크입니다.");
        }

        SignupInvitation invitation = forUpdate
                ? invitationRepository.findByIdForUpdate(invitationId)
                    .orElseThrow(() -> new UnauthorizedException("유효하지 않거나 만료된 회원가입 링크입니다."))
                : invitationRepository.findById(invitationId)
                    .orElseThrow(() -> new UnauthorizedException("유효하지 않거나 만료된 회원가입 링크입니다."));
        validateActive(invitation);
        return invitation;
    }

    private void validateActive(SignupInvitation invitation) {
        if (invitation.isRevoked()) {
            throw new UnauthorizedException("종료된 회원가입 초대입니다.");
        }
        if (invitation.isExpired(Instant.now())) {
            throw new UnauthorizedException("만료된 회원가입 링크입니다.");
        }
    }

    private void validateUnusedMember(SignupInvitationMember member) {
        if (member.isUsed()) {
            throw new BadRequestException("이미 가입을 완료한 학번입니다.");
        }
    }

    private Set<String> normalizeStudentIds(List<String> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) {
            throw new BadRequestException("가입을 허용할 학번을 입력해주세요.");
        }
        Set<String> normalized = studentIds.stream()
                .map(this::normalizeStudentId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.size() > MAX_MEMBERS) {
            throw new BadRequestException("한 초대에는 최대 500명까지 등록할 수 있습니다.");
        }
        return normalized;
    }

    private String normalizeStudentId(String studentId) {
        String normalized = studentId == null ? "" : studentId.trim();
        if (!normalized.matches("\\d{8}")) {
            throw new BadRequestException("학번은 숫자 8자리로 입력해주세요: " + normalized);
        }
        return normalized;
    }

    private void validateRegisteredStudents(Set<String> studentIds) {
        List<String> registered = userRepository.findAllByStudentIdIn(studentIds).stream()
                .map(User::getStudentId)
                .sorted()
                .toList();
        if (!registered.isEmpty()) {
            String preview = String.join(", ", registered.subList(0, Math.min(registered.size(), 10)));
            String suffix = registered.size() > 10 ? " 외 " + (registered.size() - 10) + "명" : "";
            throw new BadRequestException("이미 가입된 학번이 포함되어 있습니다: " + preview + suffix);
        }
    }

    private SignupInvitation findInvitation(UUID invitationId) {
        return invitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("회원가입 초대를 찾을 수 없습니다."));
    }

    private SignupInvitation findInvitationForUpdate(UUID invitationId) {
        return invitationRepository.findByIdForUpdate(invitationId)
                .orElseThrow(() -> new NotFoundException("회원가입 초대를 찾을 수 없습니다."));
    }

    private SignupInvitationResponseDto toResponse(
            SignupInvitation invitation,
            List<SignupInvitationMember> members
    ) {
        long totalCount = memberRepository.countByInvitationId(invitation.getId());
        long usedCount = memberRepository.countByInvitationIdAndUsedAtIsNotNull(invitation.getId());
        String token = jwtTokenProvider.generateSignupToken(invitation.getId());
        List<SignupInvitationMemberResponseDto> memberResponses = new ArrayList<>();
        for (SignupInvitationMember member : members) {
            memberResponses.add(SignupInvitationMemberResponseDto.builder()
                    .id(member.getId())
                    .studentId(member.getStudentId())
                    .userId(member.getUser() == null ? null : member.getUser().getId())
                    .userName(member.getUser() == null ? null : member.getUser().getName())
                    .usedAt(member.getUsedAt())
                    .build());
        }
        return SignupInvitationResponseDto.builder()
                .id(invitation.getId())
                .name(invitation.getName())
                .joinedQuarterId(invitation.getJoinedQuarter().getId())
                .joinedQuarterName(invitation.getJoinedQuarter().getName())
                .expiresAt(invitation.getExpiresAt())
                .revokedAt(invitation.getRevokedAt())
                .createdAt(invitation.getCreatedAt())
                .totalCount(totalCount)
                .usedCount(usedCount)
                .token(token)
                .members(memberResponses)
                .build();
    }
}
