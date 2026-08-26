package sogang.cnu.backend.about_example.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class AboutExampleReorderDto {
    @NotEmpty(message = "정렬할 소개 글 목록이 비어 있습니다.")
    private List<UUID> orderedIds;
}
