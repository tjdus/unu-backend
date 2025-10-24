package sogang.cnu.backend.quarter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@Mapper(componentModel = "spring")
public interface QuarterMapper {

    QuarterMapper INSTANCE = Mappers.getMapper(QuarterMapper.class);

    QuarterResponseDto toResponseDto(Quarter quarter);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = true)
    Quarter toEntity(QuarterRequestDto quarterRequestDto);
}
