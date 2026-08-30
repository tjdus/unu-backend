package sogang.cnu.backend.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SignupInvitationMembersRequestDto {
    @NotEmpty(message = "추가할 학번을 입력해주세요.")
    @Size(max = 500, message = "한 번에 최대 500명까지 추가할 수 있습니다.")
    private List<String> studentIds;
}
