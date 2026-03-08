package sogang.cnu.backend.common.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sogang.cnu.backend.common.domain.dto.AuditorDto;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

@Component
@RequiredArgsConstructor
public class UserAuditorMapper {

    private final UserRepository userRepository;

    public AuditorDto toAuditorDto(String username) {
        if (username == null) return null;
        return userRepository.findByUsername(username)
                .map(user -> AuditorDto.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .username(user.getUsername())
                        .studentId(user.getStudentId())
                        .build())
                .orElseGet(() -> AuditorDto.builder()
                        .username(username)
                        .build());
    }
}