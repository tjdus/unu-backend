package sogang.cnu.backend.auth.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SignupInvitationCreateRequestDto {
    @NotBlank(message = "초대 이름을 입력해주세요.")
    @Size(max = 100, message = "초대 이름은 100자 이내로 입력해주세요.")
    private String name;

    @NotNull(message = "가입 분기를 선택해주세요.")
    private UUID joinedQuarterId;

    @NotNull(message = "만료 시간을 입력해주세요.")
    @Future(message = "만료 시간은 현재보다 이후여야 합니다.")
    private Instant expiresAt;

    @NotEmpty(message = "가입을 허용할 학번을 입력해주세요.")
    @Size(max = 500, message = "한 초대에는 최대 500명까지 등록할 수 있습니다.")
    private List<String> studentIds;
}
