package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ActivityAssigneeResponseDto {
    private UUID id;
    private String name;
}
