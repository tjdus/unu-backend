package sogang.cnu.backend.form;
import org.mapstruct.Mapper;
import sogang.cnu.backend.form.dto.FormTemplateResponseDto;
@Mapper(componentModel = "spring")
public interface FormTemplateMapper {
    FormTemplateResponseDto toResponseDto(FormTemplate formTemplate);
}
