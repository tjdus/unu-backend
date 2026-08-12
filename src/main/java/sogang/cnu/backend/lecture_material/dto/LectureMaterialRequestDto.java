package sogang.cnu.backend.lecture_material.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class LectureMaterialRequestDto {
    @NotBlank(message = "자료 제목을 입력해주세요.")
    @Size(max = 120, message = "자료 제목은 120자 이내로 입력해주세요.")
    private String title;

    @Size(max = 2000, message = "자료 설명은 2000자 이내로 입력해주세요.")
    private String description;

    @Size(max = 120, message = "자료 이름은 120자 이내로 입력해주세요.")
    private String materialName;

    @NotBlank(message = "Google Drive 링크를 입력해주세요.")
    @Size(max = 2048, message = "Google Drive 링크가 너무 깁니다.")
    private String driveUrl;

    @Min(value = 1, message = "주차는 1 이상이어야 합니다.")
    private Integer weekNumber;

    private UUID activityId;
}
