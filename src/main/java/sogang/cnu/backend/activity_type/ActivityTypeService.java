package sogang.cnu.backend.activity_type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_type.dto.ActivityTypeRequestDto;
import sogang.cnu.backend.activity_type.dto.ActivityTypeResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityTypeService {
    private final ActivityTypeRepository activityTypeRepository;
    private final ActivityTypeMapper activityTypeMapper;

    @Transactional(readOnly = true)
    public ActivityTypeResponseDto getById(Integer id) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityType not found"));

        return activityTypeMapper.toResponseDto(activityType);
    }

    @Transactional(readOnly = true)
    public List<ActivityTypeResponseDto> getAll() {
        return activityTypeRepository.findAll().stream()
                .map(activityTypeMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityTypeResponseDto create(ActivityTypeRequestDto dto) {
        ActivityType activityType = activityTypeMapper.toEntity(dto);
        ActivityType savedActivityType = activityTypeRepository.save(activityType);
        return activityTypeMapper.toResponseDto(savedActivityType);
    }

    public ActivityTypeResponseDto update(Integer id, ActivityTypeRequestDto dto) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityType not found"));

        activityType.update(dto);

        ActivityType updatedActivityType = activityTypeRepository.save(activityType);
        return activityTypeMapper.toResponseDto(updatedActivityType);
    }

    public void delete(Integer id) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityType not found"));

        activityTypeRepository.delete(activityType);
    }

}
