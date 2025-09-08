package sogang.cnu.backend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sogang.cnu.backend.auth.DTO.LoginRequestDTO;
import sogang.cnu.backend.auth.DTO.LoginResponseDTO;
import sogang.cnu.backend.auth.DTO.SignUpRequestDTO;
import sogang.cnu.backend.auth.DTO.SignUpResponseDTO;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role_permission.RolePermissionRepository;
import sogang.cnu.backend.security.JwtTokenProvider;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserMapper;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.user_role.UserRole;
import sogang.cnu.backend.user_role.UserRoleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequestDto){
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        User user = UserMapper.toEntity(signUpRequestDto, encodedPassword);
        User saved = userRepository.save(user);
        return SignUpResponseDTO.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .build();
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDto){
        User user = userRepository.findByLoginId(loginRequestDto.getLoginId())
                .orElse(null);

        if(user == null || !passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            throw new RuntimeException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        List<String> roles = getUserRoles(user.getId());

        List<String> permissions = getPermissionsFromRoles(roles);

        String token = jwtTokenProvider.generateAccessToken(user.getId(), roles, permissions);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        return LoginResponseDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .build();
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new RuntimeException("리프레시 토큰이 아닙니다.");
        }

        String email = jwtTokenProvider.getIdFromToken(refreshToken);

        User user = userRepository.findByLoginId(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        List<String> roles = getUserRoles(user.getId());
        List<String> permissions = getPermissionsFromRoles(roles);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), roles, permissions);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return LoginResponseDTO.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .build();
    }

    private List<String> getUserRoles(Long userId) {
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

    private List<String> getPermissionsFromRoles(List<String> roles) {
        if (roles.isEmpty()) {
            return new ArrayList<>();
        }
        return rolePermissionRepository.findByRoleNameIn(roles)
                .stream()
                .map(rolePermission -> rolePermission.getPermission().getName())
                .toList();
    }
}
