package sogang.cnu.backend.portfolio;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    @Mapping(target = "images", source = "images")
    @Mapping(target = "tags", source = "tags")
    PortfolioResponseDto toResponseDto(Portfolio portfolio);

    default List<String> imagesToUrls(List<PortfolioImage> images) {
        if (images == null) return null;
        return images.stream()
                .sorted(Comparator.comparingInt(PortfolioImage::getSortOrder))
                .map(PortfolioImage::getUrl)
                .collect(Collectors.toList());
    }

    default List<String> tagsToNames(List<PortfolioTag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(PortfolioTag::getName)
                .collect(Collectors.toList());
    }
}
