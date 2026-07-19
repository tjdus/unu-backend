package sogang.cnu.backend.blog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.blog.dto.BlogListResponseDto;
import sogang.cnu.backend.blog.dto.BlogRequestDto;
import sogang.cnu.backend.blog.dto.BlogResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @PostMapping("")
    public ResponseEntity<BlogResponseDto> create(@RequestBody BlogRequestDto dto) {
        return ResponseEntity.ok(blogService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BlogResponseDto> update(
            @PathVariable UUID id,
            @RequestBody BlogRequestDto dto) {
        return ResponseEntity.ok(blogService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        blogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
