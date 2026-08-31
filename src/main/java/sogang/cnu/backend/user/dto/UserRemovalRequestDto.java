package sogang.cnu.backend.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class UserRemovalRequestDto {
    @NotEmpty(message = "삭제할 학회원을 선택해주세요.")
    private List<@NotNull UUID> userIds;
}
