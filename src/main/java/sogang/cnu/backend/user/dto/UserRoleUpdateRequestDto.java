package sogang.cnu.backend.user.dto;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class UserRoleUpdateRequestDto {
    private UUID userId;
    private List<String> roles;
}