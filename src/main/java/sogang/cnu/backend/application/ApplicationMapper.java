package sogang.cnu.backend.application;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.application.dto.ApplicationResponse;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface ApplicationMapper {
    @Mapping(source = "recruitment.id", target = "recruitmentId")
    ApplicationResponse toResponseDto(Application application);
}

