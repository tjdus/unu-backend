package sogang.cnu.backend.role.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class RoleResponseDto {
    private UUID id;
    private String name;
}
