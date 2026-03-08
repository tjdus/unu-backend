package sogang.cnu.backend.common.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sogang.cnu.backend.common.domain.dto.AuditorDto;
import sogang.cnu.backend.user.UserRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAuditorMapper {

    private final UserRepository userRepository;

    public AuditorDto toAuditorDto(String auditorId) {
        if (auditorId == null) return null;
        try {
            UUID id = UUID.fromString(auditorId);
            return userRepository.findById(id)
                    .map(user -> AuditorDto.builder()
                            .id(user.getId())
                            .name(user.getName())
                            .username(user.getUsername())
                            .studentId(user.getStudentId())
                            .build())
                    .orElseGet(() -> AuditorDto.builder()
                            .id(id)
                            .build());
        } catch (IllegalArgumentException e) {
            return AuditorDto.builder()
                    .username(auditorId)
                    .build();
        }
    }
}