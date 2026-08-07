package sogang.cnu.backend.blog;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BLOG_MANAGER')")
    public ResponseEntity<BlogResponseDto> create(@RequestBody BlogRequestDto dto) {
        return ResponseEntity.ok(blogService.create(dto));
    }

    // 수정/삭제는 여기서 역할을 거른 뒤, 서비스에서 작성자 본인 여부를 다시 확인한다.
    // (BLOG_MANAGER는 자기 글만, MANAGER/ADMIN은 전체)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BLOG_MANAGER')")
    public ResponseEntity<BlogResponseDto> update(
            @PathVariable UUID id,
            @RequestBody BlogRequestDto dto) {
        return ResponseEntity.ok(blogService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','BLOG_MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        blogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
