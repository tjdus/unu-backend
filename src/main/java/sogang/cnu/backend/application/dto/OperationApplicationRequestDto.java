package sogang.cnu.backend.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OperationApplicationRequestDto {
    @NotNull(message = "신청서 답변이 필요합니다.")
    private JsonNode answers;
}
