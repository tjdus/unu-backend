package sogang.cnu.backend.recruitment;

import org.mapstruct.Mapper;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.recruitment.dto.RecruitmentResponseDto;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface RecruitmentMapper {
    RecruitmentResponseDto toResponseDto(Recruitment recruitment);
}

