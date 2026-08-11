package sogang.cnu.backend.lecture_material.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LectureMaterialRequestDto {
    @NotBlank(message = "자료 제목을 입력해주세요.")
    @Size(max = 120, message = "자료 제목은 120자 이내로 입력해주세요.")
    private String title;

    @Size(max = 2000, message = "자료 설명은 2000자 이내로 입력해주세요.")
    private String description;

    @NotBlank(message = "Google Drive 링크를 입력해주세요.")
    @Size(max = 2048, message = "Google Drive 링크가 너무 깁니다.")
    private String driveUrl;
}
