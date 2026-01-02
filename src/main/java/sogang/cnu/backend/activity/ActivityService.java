package sogang.cnu.backend.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.command.ActivityCreateCommand;
import sogang.cnu.backend.activity.command.ActivityUpdateCommand;
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
        ActivityCreateCommand createCommand = toCreateCommand(dto);
        Activity activity = Activity.create(createCommand);
        Activity savedActivity = activityRepository.save(activity);
        return activityMapper.toResponseDto(savedActivity);
    }

    @Transactional
    public ActivityResponseDto update(Long id, ActivityRequestDto dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        activity.update(toUpdateCommand(dto));
        return activityMapper.toResponseDto(activity);
    }

    @Transactional
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

    private ActivityType findActivityType(Integer activityTypeId) {
        return activityTypeRepository.findById(activityTypeId)
                .orElseThrow(() -> new NotFoundException("Activity type not found"));
    }
    private User findAssignee(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Assignee not found"));
    }
    private Quarter findQuarter(Long quarterId) {
        return quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));
    }

    private ActivityCreateCommand toCreateCommand(ActivityRequestDto dto) {
        return ActivityCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(findActivityType(dto.getActivityTypeId()))
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    private ActivityUpdateCommand toUpdateCommand(ActivityRequestDto dto) {
        return ActivityUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(ActivityStatus.valueOf(dto.getStatus()))
                .activityType(findActivityType(dto.getActivityTypeId()))
                .assignee(findAssignee(dto.getAssigneeId()))
                .quarter(findQuarter(dto.getQuarterId()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

}
