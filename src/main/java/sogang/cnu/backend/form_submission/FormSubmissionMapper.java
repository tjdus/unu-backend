package sogang.cnu.backend.form_submission;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sogang.cnu.backend.common.mapper.UserAuditorMapper;
import sogang.cnu.backend.form_submission.dto.FormSubmissionResponseDto;

@Mapper(componentModel = "spring", uses = {UserAuditorMapper.class})
public interface FormSubmissionMapper {
    @Mapping(source = "form.id", target = "formId")
    @Mapping(source = "user.id", target = "userId")
    FormSubmissionResponseDto toResponseDto(FormSubmission formSubmission);
}
