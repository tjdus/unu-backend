package sogang.cnu.backend.common.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AuditorDto {
    private UUID id;
    private String name;
    private String username;
    private String studentId;
}