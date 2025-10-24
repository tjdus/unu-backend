package sogang.cnu.backend.role.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoleResponseDto {
    private Long id;
    private String name;
}
