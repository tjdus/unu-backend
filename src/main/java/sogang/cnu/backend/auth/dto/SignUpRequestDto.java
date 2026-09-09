package sogang.cnu.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequestDto {
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 50, message = "이름은 50자 이내로 입력해주세요.")
    private String name;

    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(max = 50, message = "아이디는 50자 이내로 입력해주세요.")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "아이디는 영문, 숫자, 마침표, 밑줄, 하이픈만 사용할 수 있습니다.")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하로 입력해주세요.")
    private String password;

    @NotBlank(message = "학번을 입력해주세요.")
    @Pattern(regexp = "\\d{8}", message = "학번은 숫자 8자리로 입력해주세요.")
    private String studentId;

    @NotBlank(message = "전공을 입력해주세요.")
    @Size(max = 100, message = "전공은 100자 이내로 입력해주세요.")
    private String major;

    @Size(max = 100, message = "복수·부전공은 100자 이내로 입력해주세요.")
    private String subMajor;

    @Size(max = 100, message = "GitHub ID는 100자 이내로 입력해주세요.")
    private String githubId;

    @NotBlank(message = "전화번호를 입력해주세요.")
    @Pattern(regexp = "010-\\d{4}-\\d{4}", message = "전화번호는 010-0000-0000 형식으로 입력해주세요.")
    private String phoneNumber;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 255, message = "이메일은 255자 이내로 입력해주세요.")
    private String email;

}
