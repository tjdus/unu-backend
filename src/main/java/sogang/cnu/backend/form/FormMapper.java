package sogang.cnu.backend.form;
import org.mapstruct.Mapper;
import sogang.cnu.backend.form.dto.FormResponseDto;
@Mapper(componentModel = "spring")
public interface FormMapper {
    FormResponseDto toResponseDto(Form form);
}
