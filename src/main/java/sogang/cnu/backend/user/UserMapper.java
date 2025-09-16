package sogang.cnu.backend.user;

import sogang.cnu.backend.auth.DTO.SignUpRequestDTO;
import sogang.cnu.backend.user.DTO.UserResponseDTO;

public class UserMapper {
    public static User toEntity(SignUpRequestDTO requestDTO, String encodedPassword) {
        return User.builder()
                .name(requestDTO.getName())
                .username(requestDTO.getUsername())
                .password(encodedPassword)
                .studentId(requestDTO.getStudentId())
                .githubId(requestDTO.getGithubId())
                .phoneNumber(requestDTO.getPhoneNumber())
                .email(requestDTO.getEmail())
                .build();
    }

    public static UserResponseDTO toResponseDto(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .username(user.getUsername())
                .studentId(user.getStudentId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .githubId(user.getGithubId())
                .build();
    }

}
