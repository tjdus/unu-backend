package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.PermissionChecker;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.dto.FormRequestDto;
import sogang.cnu.backend.form.dto.FormResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormRepository formRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final FormMapper formMapper;
    private final PermissionChecker permissionChecker;

    @Transactional(readOnly = true)
    public FormResponseDto getById(UUID id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Form not found"));
        return formMapper.toResponseDto(form);
    }

    @Transactional(readOnly = true)
    public List<FormResponseDto> getAll() {
        return formRepository.findAll().stream()
                .map(formMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public FormResponseDto create(FormRequestDto dto) {
        if (dto.getTemplateId() == null) {
            Form form = Form.create(dto.getTitle(), dto.getDescription(), dto.getSchema(), dto.getStartAt(), dto.getEndAt());
            Form savedForm = formRepository.save(form);
            return formMapper.toResponseDto(savedForm);
        }

        FormTemplate template = formTemplateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new NotFoundException("FormTemplate not found"));

        // If schema is not provided, copy from template
        JsonNode schema = dto.getSchema() != null ? dto.getSchema() : template.getSchema();

        Form form = Form.create(template, dto.getTitle(), dto.getDescription(), schema, dto.getStartAt(), dto.getEndAt());
        Form savedForm = formRepository.save(form);
        return formMapper.toResponseDto(savedForm);
    }

    @Transactional
    public FormResponseDto update(UUID userId, UUID id, FormRequestDto dto) {
        permissionChecker.checkManagerOrAdmin(userId);
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Form not found"));

        form.update(dto.getTitle(), dto.getDescription(), dto.getSchema(), dto.getStartAt(), dto.getEndAt());
        return formMapper.toResponseDto(form);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        permissionChecker.checkManagerOrAdmin(userId);
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Form not found"));
        formRepository.delete(form);
    }
}

