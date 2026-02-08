package sogang.cnu.backend.recruitment;

import org.mapstruct.Mapper;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

@Mapper(componentModel = "spring")
public interface RecruitmentMapper {
    RecruitmentResponseDto toResponseDto(Recruitment recruitment);
}

