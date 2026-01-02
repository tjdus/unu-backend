package sogang.cnu.backend.activity_type;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_type.command.ActivityTypeCreateCommand;
import sogang.cnu.backend.activity_type.command.ActivityTypeUpdateCommand;
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
        ActivityType activityType = ActivityType.create(toCreateCommand(dto));
        ActivityType savedActivityType = activityTypeRepository.save(activityType);
        return activityTypeMapper.toResponseDto(savedActivityType);
    }

    @Transactional
    public ActivityTypeResponseDto update(Integer id, ActivityTypeRequestDto dto) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityType not found"));

        ActivityTypeUpdateCommand updateCommand = toUpdateCommand(dto);
        activityType.update(updateCommand);
        return activityTypeMapper.toResponseDto(activityType);
    }

    @Transactional
    public void delete(Integer id) {
        ActivityType activityType = activityTypeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityType not found"));

        activityTypeRepository.delete(activityType);
    }

    private ActivityTypeCreateCommand toCreateCommand(ActivityTypeRequestDto dto) {
        return ActivityTypeCreateCommand.builder()
                .name(dto.getName())
                .build();
    }

    private ActivityTypeUpdateCommand toUpdateCommand(ActivityTypeRequestDto dto) {
        return ActivityTypeUpdateCommand.builder()
                .name(dto.getName())
                .build();
    }

}
