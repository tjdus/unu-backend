package sogang.cnu.backend.about_example.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.about_example.AboutExampleCategory;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AboutExampleResponseDto {
    private UUID id;
    private AboutExampleCategory category;
    private String title;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
