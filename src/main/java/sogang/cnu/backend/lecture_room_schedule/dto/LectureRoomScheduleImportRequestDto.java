package sogang.cnu.backend.lecture_room_schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class LectureRoomScheduleImportRequestDto {
    @NotNull(message = "분기를 선택해주세요.")
    private UUID quarterId;

    @Valid
    @NotEmpty(message = "반영할 응답이 없습니다.")
    @Size(max = 500, message = "한 번에 최대 500명까지 반영할 수 있습니다.")
    private List<UserSchedule> users = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    public static class UserSchedule {
        @NotBlank(message = "학번을 입력해주세요.")
        @Pattern(regexp = "\\d{8}", message = "학번은 8자리 숫자여야 합니다.")
        private String studentId;

        @Valid
        @NotNull(message = "관리 가능 시간 목록이 필요합니다.")
        @Size(max = 40, message = "한 사용자는 최대 40개 시간만 등록할 수 있습니다.")
        private List<Slot> slots = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    public static class Slot {
        @NotBlank(message = "요일을 입력해주세요.")
        private String dayOfWeek;

        @NotNull(message = "교시를 입력해주세요.")
        @Min(value = 1, message = "교시는 1교시 이상이어야 합니다.")
        @Max(value = 8, message = "교시는 8교시 이하여야 합니다.")
        private Integer period;
    }
}
