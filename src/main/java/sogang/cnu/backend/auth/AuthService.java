package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sogang.cnu.backend.auth.dto.*;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role.RoleRepository;
import sogang.cnu.backend.security.JwtTokenProvider;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserMapper;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user.command.UserCreateCommand;
import sogang.cnu.backend.user.command.UserUpdateCommand;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user_role.UserRole;
import sogang.cnu.backend.user_role.UserRoleRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final QuarterRepository quarterRepository;
    private final RoleRepository roleRepository;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto, String token){
        if (!jwtTokenProvider.validateToken(token) || !jwtTokenProvider.isSignupToken(token)) {
            throw new RuntimeException("유효하지 않은 회원가입 토큰입니다.");
        }

        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        UserCreateCommand createCommand = toCreateCommand(signUpRequestDto);
        createCommand.setPassword(encodedPassword);

        User user = User.create(createCommand);

        User savedUser = userRepository.save(user);

        Role roleMember = roleRepository.findByName("MEMBER")
                .orElseThrow(() -> new RuntimeException("권한이 존재하지 않습니다."));

        userRoleRepository.save(
                UserRole.builder()
                        .user(savedUser)
                        .role(roleMember)
                        .build()
        );


        return SignUpResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto){
        User user = userRepository.findByUsername(loginRequestDto.getUsername())
                .orElse(null);

        if(user == null || !passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        List<String> roles = getUserRoles(user.getId());

        String token = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return LoginResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .build();
    }

    public LoginResponseDto refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 아닙니다.");
        }

        String email = jwtTokenProvider.getIdFromToken(refreshToken);

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        List<String> roles = getUserRoles(user.getId());

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();
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

        String encodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    public UserInfoResponseDto getUserInfo(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toInfoResponseDto(user);
    }

    public SignupTokenResponseDto generateSignupToken() {
        String token = jwtTokenProvider.generateSignupToken();
        return SignupTokenResponseDto.builder()
                .token(token)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
    }

    private UserCreateCommand toCreateCommand(SignUpRequestDto dto) {

        return UserCreateCommand.builder()
                .name(dto.getName())
                .username(dto.getUsername())
                .password(dto.getPassword())
                .studentId(dto.getStudentId())
                .githubId(dto.getGithubId())
                .phoneNumber(dto.getPhoneNumber())
                .joinedQuarter(quarterRepository.findById(dto.getJoinedQuarterId())
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 분기입니다.")))
                .email(dto.getEmail())
                .isActive(true)
                .build();
    }
}
