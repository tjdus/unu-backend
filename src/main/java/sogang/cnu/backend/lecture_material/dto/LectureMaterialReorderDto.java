package sogang.cnu.backend.lecture_material.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class LectureMaterialReorderDto {
    @NotEmpty(message = "정렬할 강의자료 목록이 비어 있습니다.")
    private List<UUID> orderedIds;
}
