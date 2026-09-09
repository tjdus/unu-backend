package sogang.cnu.backend.blog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.blog.command.BlogCreateCommand;
import sogang.cnu.backend.blog.command.BlogUpdateCommand;
import sogang.cnu.backend.blog.dto.BlogListResponseDto;
import sogang.cnu.backend.blog.dto.BlogRequestDto;
import sogang.cnu.backend.blog.dto.BlogResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.image.ImageService;
import sogang.cnu.backend.image.PostType;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final ImageService imageService;

    @Transactional(readOnly = true)
    public BlogListResponseDto getAll(String category) {
        List<Blog> blogs;
        if (category != null && !category.isBlank()) {
            blogs = blogRepository.findAllByCategoryOrderByCreatedAtDesc(
                    BlogCategory.valueOf(category.toUpperCase()));
        } else {
            blogs = blogRepository.findAllByOrderByCreatedAtDesc();
        }
        List<BlogResponseDto> posts = blogs.stream()
                .map(blogMapper::toResponseDto)
                .collect(Collectors.toList());
        return BlogListResponseDto.builder()
                .posts(posts)
                .total(posts.size())
                .build();
    }

    @Transactional(readOnly = true)
    public BlogResponseDto getById(UUID id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        return blogMapper.toResponseDto(blog);
    }

    @Transactional
    public BlogResponseDto create(BlogRequestDto dto) {
        String thumbnailUrl = imageService.syncImages(
                null, PostType.BLOG, dto.getDescription(), dto.getThumbnailUrl()
        );

        Blog blog = Blog.create(BlogCreateCommand.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .category(dto.getCategory() != null ? BlogCategory.valueOf(dto.getCategory().toUpperCase()) : null)
                .build());
        Blog saved = blogRepository.save(blog);

        imageService.syncImages(saved.getId(), PostType.BLOG, dto.getDescription(), thumbnailUrl);

        return blogMapper.toResponseDto(saved);
    }

    @Transactional
    public BlogResponseDto update(UUID id, BlogRequestDto dto) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        SecurityUtils.requireOwnerOrAdmin(blog.getCreatedBy(), "본인이 작성한 글만 수정할 수 있습니다.");

        String thumbnailUrl = imageService.syncImages(
                id, PostType.BLOG, dto.getDescription(), dto.getThumbnailUrl()
        );

        blog.update(BlogUpdateCommand.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .description(dto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .category(dto.getCategory() != null ? BlogCategory.valueOf(dto.getCategory().toUpperCase()) : null)
                .build());

        return blogMapper.toResponseDto(blog);
    }

    @Transactional
    public void delete(UUID id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        SecurityUtils.requireOwnerOrAdmin(blog.getCreatedBy(), "본인이 작성한 글만 삭제할 수 있습니다.");
        imageService.deletePostImages(id, PostType.BLOG);
        blogRepository.delete(blog);
    }
}
