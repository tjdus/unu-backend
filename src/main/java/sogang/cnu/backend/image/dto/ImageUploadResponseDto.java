package sogang.cnu.backend.image.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ImageUploadResponseDto {
    private UUID id;
    private String url;
}
