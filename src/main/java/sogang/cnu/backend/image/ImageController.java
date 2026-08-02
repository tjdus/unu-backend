package sogang.cnu.backend.image;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sogang.cnu.backend.image.dto.ImageUploadResponseDto;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponseDto> upload(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) return ResponseEntity.badRequest().build();

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(imageService.upload(file));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        imageService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
