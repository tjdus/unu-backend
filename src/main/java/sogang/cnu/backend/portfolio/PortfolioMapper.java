package sogang.cnu.backend.portfolio;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    @Mapping(target = "startQuarterId", expression = "java(portfolio.getStartQuarter() != null ? portfolio.getStartQuarter().getId().toString() : null)")
    @Mapping(target = "startQuarterName", source = "startQuarter.name")
    @Mapping(target = "endQuarterId", expression = "java(portfolio.getEndQuarter() != null ? portfolio.getEndQuarter().getId().toString() : null)")
    @Mapping(target = "endQuarterName", source = "endQuarter.name")
    @Mapping(target = "contributors", ignore = true)
    @Mapping(target = "createdAt", expression = "java(portfolio.getCreatedAt() != null ? portfolio.getCreatedAt().toString() : null)")
    PortfolioResponseDto toResponseDto(Portfolio portfolio);
}
