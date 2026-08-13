package sogang.cnu.backend.activity_opening_request.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ActivityOpeningRequestDto {
    @NotBlank(message = "활동명을 입력해주세요.")
    @Size(max = 100, message = "활동명은 100자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "활동 소개를 입력해주세요.")
    @Size(max = 3000, message = "활동 소개는 3000자 이내로 입력해주세요.")
    private String description;

    @NotBlank(message = "운영 계획을 입력해주세요.")
    @Size(max = 10000, message = "운영 계획은 10000자 이내로 입력해주세요.")
    private String operationPlan;

    @NotNull(message = "활동 유형을 선택해주세요.")
    private UUID activityTypeId;

    @NotNull(message = "분기를 선택해주세요.")
    private UUID quarterId;

    @NotNull(message = "시작일을 입력해주세요.")
    private LocalDate startDate;

    @NotNull(message = "종료일을 입력해주세요.")
    private LocalDate endDate;

    @NotNull(message = "예상 인원을 입력해주세요.")
    @Min(value = 1, message = "예상 인원은 1명 이상이어야 합니다.")
    @Max(value = 100, message = "예상 인원은 100명 이하로 입력해주세요.")
    private Integer expectedMemberCount;

    @NotNull(message = "신규 학회원 모집 여부를 선택해주세요.")
    private Boolean acceptsNewMembers;

    @Min(value = 1, message = "참여 정원은 1명 이상이어야 합니다.")
    @Max(value = 1000, message = "참여 정원은 1000명 이하로 입력해주세요.")
    private Integer participantLimit;

    private String recruitmentPositions;

    private String instructorCareer;

    @NotNull(message = "개인 프로젝트 여부를 선택해주세요.")
    private Boolean personalProject;

    private UUID parentActivityId;

    private Set<UUID> initialMemberIds = new LinkedHashSet<>();
}
