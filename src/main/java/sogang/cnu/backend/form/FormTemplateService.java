package sogang.cnu.backend.form;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.dto.FormTemplateRequestDto;
import sogang.cnu.backend.form.dto.FormTemplateResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormTemplateService {
    private final FormTemplateRepository formTemplateRepository;
    private final FormTemplateMapper formTemplateMapper;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public FormTemplateResponseDto getById(UUID id) {
        FormTemplate formTemplate = formTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormTemplate not found"));
        return formTemplateMapper.toResponseDto(formTemplate);
    }

    @Transactional(readOnly = true)
    public List<FormTemplateResponseDto> getAll() {
        return formTemplateRepository.findAll().stream()
                .map(formTemplateMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FormTemplateResponseDto create(FormTemplateRequestDto dto) {
        FormTemplate formTemplate = FormTemplate.create(dto.getTitle(), dto.getDescription(), dto.getSchema());
        FormTemplate savedFormTemplate = formTemplateRepository.save(formTemplate);
        return formTemplateMapper.toResponseDto(savedFormTemplate);
    }

    @Transactional
    public FormTemplateResponseDto update(UUID userId, UUID id, FormTemplateRequestDto dto) {
        permissionChecker.checkManagerOrAdmin(userId);
        FormTemplate formTemplate = formTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormTemplate not found"));

        formTemplate.update(dto.getTitle(), dto.getDescription(), dto.getSchema());
        return formTemplateMapper.toResponseDto(formTemplate);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        permissionChecker.checkManagerOrAdmin(userId);
        FormTemplate formTemplate = formTemplateRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("FormTemplate not found"));
        formTemplateRepository.delete(formTemplate);
    }
}

