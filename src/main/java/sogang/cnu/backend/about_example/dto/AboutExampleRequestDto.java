package sogang.cnu.backend.about_example.dto;

import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.about_example.AboutExampleCategory;

@Getter
@Setter
public class AboutExampleRequestDto {
    private AboutExampleCategory category;
    private String title;
    private String description;
    private String thumbnailUrl;
}
