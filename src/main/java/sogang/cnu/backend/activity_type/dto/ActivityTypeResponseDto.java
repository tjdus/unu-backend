package sogang.cnu.backend.activity_type.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ActivityTypeResponseDto {
    private Integer id;
    private String name;
}
