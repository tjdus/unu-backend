package sogang.cnu.backend.activity_type.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ActivityTypeResponseDto {
    private UUID id;
    private String name;
    private String code;
}
