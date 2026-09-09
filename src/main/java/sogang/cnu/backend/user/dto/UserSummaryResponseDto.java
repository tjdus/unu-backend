package sogang.cnu.backend.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserSummaryResponseDto {
    private UUID id;
    private String name;
    private String studentId;
}
