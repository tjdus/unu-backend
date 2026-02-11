package sogang.cnu.backend.activity_session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_session.command.ActivitySessionCreateCommand;
import sogang.cnu.backend.activity_session.command.ActivitySessionUpdateCommand;
import sogang.cnu.backend.activity_session.dto.ActivitySessionRequestDto;
import sogang.cnu.backend.activity_session.dto.ActivitySessionResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivitySessionService {
    private final ActivitySessionRepository activitySessionRepository;
    private final ActivitySessionMapper activitySessionMapper;
    private final ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public ActivitySessionResponseDto getById(Long id) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));

        return activitySessionMapper.toResponseDto(activitySession);
    }

    @Transactional(readOnly = true)
    public List<ActivitySessionResponseDto> getAll() {
        return activitySessionRepository.findAll().stream()
                .map(activitySessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivitySessionResponseDto create(ActivitySessionRequestDto dto) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        ActivitySessionCreateCommand createCommand = ActivitySessionCreateCommand.builder()
                .activity(activity)
                .sessionNumber(dto.getSessionNumber())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();

        ActivitySession activitySession = ActivitySession.create(createCommand);
        ActivitySession savedActivitySession = activitySessionRepository.save(activitySession);
        return activitySessionMapper.toResponseDto(savedActivitySession);
    }


    @Transactional
    public ActivitySessionResponseDto update(Long id, ActivitySessionRequestDto dto) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));
        ActivitySessionUpdateCommand updateCommand = ActivitySessionUpdateCommand.builder()
                .sessionNumber(dto.getSessionNumber())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();
        activitySession.update(updateCommand);
        return activitySessionMapper.toResponseDto(activitySession);
    }

    @Transactional
    public void delete(Long id) {
        ActivitySession activitySession = activitySessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivitySession not found"));
        activitySessionRepository.delete(activitySession);
    }

    @Transactional(readOnly = true)
    public List<ActivitySessionResponseDto> getByActivityId(Long activityId) {
        return activitySessionRepository.findByActivityId(activityId).stream()
                .map(activitySessionMapper::toResponseDto)
                .collect(Collectors.toList());
    }
}
