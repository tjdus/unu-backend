package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.dto.ActivityMappingDto;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;
import sogang.cnu.backend.activity_type.ActivityType;
import sogang.cnu.backend.activity_type.ActivityTypeRepository;
import sogang.cnu.backend.common.exception.NotFoundException;

import sogang.cnu.backend.activity.dto.ActivityRequestDto;
import sogang.cnu.backend.activity.dto.ActivityResponseDto;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final ActivityMapper activityMapper;
    private final UserRepository userRepository;
    private final ActivityTypeRepository activityTypeRepository;
    private final QuarterRepository quarterRepository;
    private final ActivityRepositoryCustom activityRepositoryCustom;

    @Transactional(readOnly = true)
    public ActivityResponseDto getById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        return activityMapper.toResponseDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> getAll() {
        return activityRepository.findAll().stream()
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityResponseDto create(ActivityRequestDto dto) {
        ActivityMappingDto mappingDto = toMappingDto(dto);
        Activity activity = activityMapper.toEntity(mappingDto);
        Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toResponseDto(savedActivity);
    }

    private ActivityMappingDto toMappingDto(ActivityRequestDto dto) {
        ActivityType activityType = activityTypeRepository.findById(dto.getActivityTypeId())
                .orElseThrow(() -> new NotFoundException("Activity type not found"));
        User assignee = userRepository.findById(dto.getAssigneeId())
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
        Quarter quarter = quarterRepository.findById(dto.getQuarterId())
                .orElseThrow(() -> new NotFoundException("Quarter not found"));

        return ActivityMappingDto.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .activityType(activityType)
                .assignee(assignee)
                .quarter(quarter)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public ActivityResponseDto update(Long id, ActivityRequestDto dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        ActivityMappingDto mappingDto = toMappingDto(dto);

        activity.update(mappingDto);

        Activity updatedActivity = activityRepository.save(activity);
        return activityMapper.toResponseDto(updatedActivity);
    }

    public void delete(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        activityRepository.delete(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponseDto> search(ActivitySearchQuery query) {
        return activityRepositoryCustom.search(query).stream()
                .map(activityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

}
