package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.form.dto.FormRequestDto;
import sogang.cnu.backend.form.dto.FormResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {
    private final FormRepository formRepository;
    private final FormTemplateRepository formTemplateRepository;
    private final FormMapper formMapper;

    @Transactional(readOnly = true)
    public FormResponseDto getById(Long id) {
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
        FormTemplate template = formTemplateRepository.findById(dto.getTemplateId())
                .orElseThrow(() -> new NotFoundException("FormTemplate not found"));

        // If schema is not provided, copy from template
        JsonNode schema = dto.getSchema() != null ? dto.getSchema() : template.getSchema();

        Form form = Form.create(template, dto.getTitle(), schema);
        Form savedForm = formRepository.save(form);
        return formMapper.toResponseDto(savedForm);
    }

    @Transactional
    public FormResponseDto update(Long id, FormRequestDto dto) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Form not found"));

        form.update(dto.getTitle(), dto.getSchema());
        return formMapper.toResponseDto(form);
    }

    @Transactional
    public void delete(Long id) {
        Form form = formRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Form not found"));
        formRepository.delete(form);
    }
}

