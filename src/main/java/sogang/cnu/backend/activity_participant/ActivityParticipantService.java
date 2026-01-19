package sogang.cnu.backend.activity_participant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.*;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantCreateCommand;
import sogang.cnu.backend.activity_participant.command.ActivityParticipantUpdateCommand;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantRequestDto;
import sogang.cnu.backend.activity_participant.dto.ActivityParticipantResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityParticipantService {
    private final ActivityParticipantRepository activityParticipantRepository;
    private final ActivityParticipantMapper activityParticipantMapper;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;


    @Transactional(readOnly = true)
    public ActivityParticipantResponseDto getById(Long id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityParticipantResponseDto> getAll() {
        return activityParticipantRepository.findAll().stream()
                .map(activityParticipantMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ActivityParticipantResponseDto create(ActivityParticipantRequestDto dto) {
        ActivityParticipantCreateCommand createCommand = toCreateCommand(dto);
        ActivityParticipant activityParticipant = ActivityParticipant.create(createCommand);
        return activityParticipantMapper.toResponseDto(activityParticipant);
    }

    @Transactional
    public ActivityParticipantResponseDto participate(Long userId, ActivityParticipantRequestDto dto) {
        dto.setUserId(userId);
        ActivityParticipantCreateCommand createCommand = toCreateCommand(dto);
        ActivityParticipant activityParticipant = ActivityParticipant.create(createCommand);
        return activityParticipantMapper.toResponseDto(activityParticipant);
    }

    @Transactional
    public ActivityParticipantResponseDto update(Long id, ActivityParticipantRequestDto dto) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        activity.update(toUpdateCommand(dto));
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public ActivityParticipantResponseDto updateStatus(Long id, ActivityParticipantRequestDto dto) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        activity.updateStatus(ActivityParticipantStatus.valueOf(dto.getStatus()));
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public ActivityParticipantResponseDto updateCompleted(Long id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));

        activity.updateCompleted();
        return activityParticipantMapper.toResponseDto(activity);
    }

    @Transactional
    public void delete(Long id) {
        ActivityParticipant activity = activityParticipantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ActivityParticipant not found"));
        activityParticipantRepository.delete(activity);
    }

    private Activity findActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("Activity not found"));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private ActivityParticipantCreateCommand toCreateCommand(ActivityParticipantRequestDto dto) {
        return ActivityParticipantCreateCommand.builder()
                .activity(findActivity(dto.getActivityId()))
                .user(findUser(dto.getUserId()))
                .status(ActivityParticipantStatus.valueOf(dto.getStatus()))
                .build();
    }

    private ActivityParticipantUpdateCommand toUpdateCommand(ActivityParticipantRequestDto dto) {
        return ActivityParticipantUpdateCommand.builder()
                .status(ActivityParticipantStatus.valueOf(dto.getStatus()))
                .build();
    }

}
