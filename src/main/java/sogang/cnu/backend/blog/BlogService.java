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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;

    @Transactional(readOnly = true)
    public BlogListResponseDto getAll(String category) {
        List<Blog> blogs;
        if (category != null && !category.isBlank()) {
            BlogCategory blogCategory = BlogCategory.valueOf(category.toUpperCase());
            blogs = blogRepository.findAllByCategoryOrderByCreatedAtDesc(blogCategory);
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
        BlogCreateCommand command = toCreateCommand(dto);
        Blog blog = Blog.create(command);
        Blog saved = blogRepository.save(blog);
        return blogMapper.toResponseDto(saved);
    }

    @Transactional
    public BlogResponseDto update(UUID id, BlogRequestDto dto) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blog.update(toUpdateCommand(dto));
        return blogMapper.toResponseDto(blog);
    }

    @Transactional
    public void delete(UUID id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog not found"));
        blogRepository.delete(blog);
    }

    private BlogCreateCommand toCreateCommand(BlogRequestDto dto) {
        return BlogCreateCommand.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .content(dto.getContent())
                .thumbnailUrl(dto.getThumbnailUrl())
                .category(BlogCategory.valueOf(dto.getCategory().toUpperCase()))
                .build();
    }

    private BlogUpdateCommand toUpdateCommand(BlogRequestDto dto) {
        return BlogUpdateCommand.builder()
                .title(dto.getTitle())
                .subtitle(dto.getSubtitle())
                .content(dto.getContent())
                .thumbnailUrl(dto.getThumbnailUrl())
                .category(BlogCategory.valueOf(dto.getCategory().toUpperCase()))
                .build();
    }
}
