package sogang.cnu.backend.user;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.role.RoleRepository;
import sogang.cnu.backend.user.dto.UserResponseDto;
import sogang.cnu.backend.user.dto.UserRoleUpdateRequestDto;
import sogang.cnu.backend.user_role.UserRole;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserRepositoryCustom userRepositoryCustom;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public UserResponseDto getByStudentId(String studentId) {
        User user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return userMapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> search(String role, Boolean isActive, String joinedQuarter, String name, String studentId) {
        List<User> users = userRepositoryCustom.search(role, isActive, joinedQuarter, name, studentId);
        return users.stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDto changeUserRole(UserRoleUpdateRequestDto request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Role> newRoles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + roleName)))
                .toList();

        user.getUserRoles().clear();
        entityManager.flush();

        List<UserRole> userRoles = newRoles.stream()
                .map(role -> UserRole.builder().user(user).role(role).build())
                .collect(Collectors.toList());
        user.getUserRoles().addAll(userRoles);

        return userMapper.toResponseDto(user);
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Transactional
    public UserResponseDto updateUserActiveStatus(UUID id, boolean isActive) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        user.updateActiveStatus(isActive);
        return userMapper.toResponseDto(user);
    }

}
