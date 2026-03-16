package sogang.cnu.backend.quarter;

import org.mapstruct.Mapper;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@Mapper(componentModel = "spring")
public interface QuarterMapper {
    QuarterResponseDto toResponseDto(Quarter quarter);
}
