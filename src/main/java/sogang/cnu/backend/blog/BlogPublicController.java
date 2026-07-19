package sogang.cnu.backend.blog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.blog.dto.BlogListResponseDto;
import sogang.cnu.backend.blog.dto.BlogRequestDto;
import sogang.cnu.backend.blog.dto.BlogResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/blogs")
@RequiredArgsConstructor
public class BlogPublicController {

    private final BlogService blogService;

    @GetMapping("")
    public ResponseEntity<BlogListResponseDto> getAll(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(blogService.getAll(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(blogService.getById(id));
    }
}
