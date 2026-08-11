package sogang.cnu.backend.lecture_material.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class LectureMaterialResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String driveUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
