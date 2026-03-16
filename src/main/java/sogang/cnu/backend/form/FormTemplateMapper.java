package sogang.cnu.backend.form;
import org.mapstruct.Mapper;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.form.dto.FormTemplateResponseDto;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface FormTemplateMapper {
    FormTemplateResponseDto toResponseDto(FormTemplate formTemplate);
}
