package sogang.cnu.backend.blog;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.blog.dto.BlogResponseDto;

@Mapper(componentModel = "spring")
public interface BlogMapper {

    @Mapping(target = "category", expression = "java(blog.getCategory() != null ? blog.getCategory().name() : null)")
    BlogResponseDto toResponseDto(Blog blog);
}
