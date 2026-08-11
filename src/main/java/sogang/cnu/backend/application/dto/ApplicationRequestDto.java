package sogang.cnu.backend.application.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ApplicationRequestDto {
    @NotNull(message = "모집 정보가 필요합니다.")
    private UUID recruitmentId;
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    private String name;
    @NotBlank(message = "학번을 입력해주세요.")
    @Pattern(regexp = "\\d{8}", message = "학번은 숫자 8자리로 입력해주세요.")
    private String studentId;
    @NotBlank(message = "전공을 입력해주세요.")
    @Size(max = 100, message = "전공은 100자 이하여야 합니다.")
    private String major;
    @Size(max = 100, message = "부전공은 100자 이하여야 합니다.")
    private String subMajor;
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다.")
    private String email;
    @Size(max = 100, message = "GitHub ID는 100자 이하여야 합니다.")
    private String githubId;
    @NotBlank(message = "연락처를 입력해주세요.")
    @Pattern(regexp = "\\d{3}-\\d{4}-\\d{4}", message = "연락처는 000-0000-0000 형식으로 입력해주세요.")
    private String phoneNumber;
    @NotNull(message = "지원서 답변이 필요합니다.")
    private JsonNode answers;

    @Size(min = 6, max = 100, message = "비밀번호는 6자 이상 100자 이하여야 합니다.")
    private String password;
}
