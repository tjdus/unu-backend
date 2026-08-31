package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.auth.dto.*;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role.RoleRepository;
import sogang.cnu.backend.security.JwtTokenProvider;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.MemberStatus;
import sogang.cnu.backend.user.UserMapper;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.DuplicateUserException;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user.command.UserCreateCommand;
import sogang.cnu.backend.user.command.UserUpdateCommand;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user_role.UserRole;
import sogang.cnu.backend.user_role.UserRoleRepository;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.UnauthorizedException;
import sogang.cnu.backend.util.SecurityUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final SignupInvitationService signupInvitationService;

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto, String token){
        normalizeSignupRequest(signUpRequestDto);
        SignupInvitationMember invitationMember = signupInvitationService
                .findEligibleMemberForSignup(token, signUpRequestDto.getStudentId());
        validateAccountPassword(signUpRequestDto.getPassword());
        validateSignupUniqueness(signUpRequestDto);
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        UserCreateCommand createCommand = toCreateCommand(
                signUpRequestDto,
                invitationMember.getInvitation().getJoinedQuarter()
        );
        createCommand.setPassword(encodedPassword);

        User user = User.create(createCommand);
        Role roleMember = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new RuntimeException("권한이 존재하지 않습니다."));

        User savedUser;
        try {
            savedUser = userRepository.saveAndFlush(user);
            userRoleRepository.saveAndFlush(
                    UserRole.builder()
                            .user(savedUser)
                            .role(roleMember)
                            .build()
            );
            invitationMember.markUsed(savedUser, Instant.now());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException("이미 사용 중인 회원 정보가 있습니다.");
        }


        return SignUpResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public SignupEligibilityResponseDto verifySignupEligibility(String token, String studentId) {
        return signupInvitationService.verifyEligibility(token, studentId);
    }

    public AuthResult login(LoginRequestDto loginRequestDto){
        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElse(null);

        if(user == null
                || user.getMemberStatus() == MemberStatus.REMOVED
                || !passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        List<String> roles = getUserRoles(user.getId());

        String token = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return new AuthResult(token, refreshToken, user.getEmail());
    }

    // 슬라이딩 방식: 검증 성공 시 새 access/refresh 토큰을 함께 재발급한다(rotation 유지).
    public AuthResult refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("세션이 만료되었습니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("세션이 만료되었습니다.");
        }

        UUID userId;
        try {
            userId = UUID.fromString(jwtTokenProvider.getIdFromToken(refreshToken));
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("세션이 만료되었습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("세션이 만료되었습니다."));
        if (user.getMemberStatus() == MemberStatus.REMOVED) {
            throw new UnauthorizedException("세션이 만료되었습니다.");
        }

        List<String> roles = getUserRoles(user.getId());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return new AuthResult(newAccessToken, newRefreshToken, user.getEmail());
    }

    private List<String> getUserRoles(UUID userId) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(userId);
        List<String> roles = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            Role role = userRole.getRole();
            if (role != null) {
                roles.add(role.getName());
            }
        }
        return roles;
    }

    public UserResponseDto update(UUID id, UserInfoRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!SecurityUtils.isAdmin()
                && (!Objects.equals(user.getName(), dto.getName())
                || !Objects.equals(user.getStudentId(), dto.getStudentId()))) {
            throw new ForbiddenException("이름과 학번은 관리자만 변경할 수 있습니다.");
        }

        validateUpdate(id, dto);

        user.update(UserUpdateCommand.builder()
                        .name(dto.getName())
                        .username(dto.getUsername())
                        .studentId(dto.getStudentId())
                        .githubId(dto.getGithubId())
                        .phoneNumber(dto.getPhoneNumber())
                        .email(dto.getEmail())
                .build());
        userRepository.save(user);

        return userMapper.toResponseDto(user);
    }

    public void updatePassword(UUID id, PasswordUpdateRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }

        validateAccountPassword(dto.getNewPassword());
        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    public UserInfoResponseDto getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toInfoResponseDto(user);
    }

    public ResetPasswordResponseDto resetPassword(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        String temporaryPassword = generateTemporaryPassword();
        user.updatePassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(user);

        return ResetPasswordResponseDto.builder()
                .temporaryPassword(temporaryPassword)
                .build();
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void normalizeSignupRequest(SignUpRequestDto dto) {
        dto.setName(dto.getName().trim());
        dto.setUsername(dto.getUsername().trim());
        dto.setStudentId(dto.getStudentId().trim());
        dto.setMajor(dto.getMajor().trim());
        dto.setSubMajor(normalizeOptional(dto.getSubMajor()));
        dto.setPhoneNumber(dto.getPhoneNumber().trim());
        dto.setEmail(dto.getEmail().trim().toLowerCase(Locale.ROOT));
        dto.setGithubId(normalizeOptional(dto.getGithubId()));
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private void validateSignupUniqueness(SignUpRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateUserException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByStudentId(dto.getStudentId())) {
            throw new DuplicateUserException("이미 가입된 학번입니다.");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new DuplicateUserException("이미 사용 중인 전화번호입니다.");
        }
        if (dto.getGithubId() != null && userRepository.existsByGithubId(dto.getGithubId())) {
            throw new DuplicateUserException("이미 사용 중인 GitHub ID입니다.");
        }
    }

    private UserCreateCommand toCreateCommand(SignUpRequestDto dto, Quarter joinedQuarter) {

        return UserCreateCommand.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .studentId(dto.getStudentId())
                .major(dto.getMajor())
                .subMajor(dto.getSubMajor())
                .githubId(dto.getGithubId())
                .phoneNumber(dto.getPhoneNumber())
                .joinedQuarter(joinedQuarter)
                .email(dto.getEmail())
                .isCurrentQuarterActive(true)
                .build();
    }

    private void validateUpdate(UUID id, UserInfoRequestDto dto) {
        if (dto.getName() == null || dto.getName().isBlank())
            throw new BadRequestException("이름은 필수입니다.");
        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new BadRequestException("사용자 이름은 필수입니다.");
        if (dto.getStudentId() == null || dto.getStudentId().isBlank())
            throw new BadRequestException("학번은 필수입니다.");

        if (userRepository.existsByUsernameAndIdNot(dto.getUsername(), id))
            throw new BadRequestException("이미 사용 중인 사용자 이름입니다.");
        if (userRepository.existsByStudentIdAndIdNot(dto.getStudentId(), id))
            throw new BadRequestException("이미 사용 중인 학번입니다.");
        if (dto.getEmail() != null && userRepository.existsByEmailAndIdNot(dto.getEmail(), id))
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        if (dto.getGithubId() != null && !dto.getGithubId().isBlank()
                && userRepository.existsByGithubIdAndIdNot(dto.getGithubId(), id))
            throw new BadRequestException("이미 사용 중인 GitHub 아이디입니다.");
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()
                && userRepository.existsByPhoneNumberAndIdNot(dto.getPhoneNumber(), id))
            throw new BadRequestException("이미 사용 중인 전화번호입니다.");
    }

    private void validateAccountPassword(String password) {
        if (password == null || password.length() < 8 || password.length() > 100) {
            throw new BadRequestException("비밀번호는 8자 이상 100자 이하로 입력해주세요.");
        }
    }
}
