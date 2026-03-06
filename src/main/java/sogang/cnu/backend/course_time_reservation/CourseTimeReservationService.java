package sogang.cnu.backend.course_time_reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity.Activity;
import sogang.cnu.backend.activity.ActivityRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.activity_participant.ActivityParticipantRepository;
import sogang.cnu.backend.activity_participant.ActivityParticipantStatus;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.ForbiddenException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.common.exception.ReservationConflictException;
import sogang.cnu.backend.course_time_reservation.command.CourseTimeReservationCreateCommand;
import sogang.cnu.backend.course_time_reservation.command.CourseTimeReservationUpdateCommand;
import sogang.cnu.backend.course_time_reservation.dto.CourseTimeReservationRequestDto;
import sogang.cnu.backend.course_time_reservation.dto.CourseTimeReservationResponseDto;
import sogang.cnu.backend.security.CustomUserDetails;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseTimeReservationService {

    private static final int MAX_SINGLE_MINUTES = 240;  // 4 hours
    private static final int MAX_DAILY_MINUTES = 360;   // 6 hours

    private final CourseTimeReservationRepository reservationRepository;
    private final CourseTimeReservationMapper reservationMapper;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseTimeReservationResponseDto create(CourseTimeReservationRequestDto dto, CustomUserDetails currentUser) {
        Activity activity = activityRepository.findById(dto.getActivityId())
                .orElseThrow(() -> new NotFoundException("Activity not found"));

        validateOnlineCourse(activity);

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        validateParticipant(user.getId(), activity.getId());
        validateTimeRange(dto.getStartAt(), dto.getEndAt());
        validateSingleDuration(dto.getStartAt(), dto.getEndAt());
        validateNoOverlap(user.getId(), dto.getStartAt(), dto.getEndAt(), null);
        validateDailyLimit(user.getId(), dto.getStartAt(), dto.getEndAt(), null);

        CourseTimeReservation reservation = CourseTimeReservation.create(
                CourseTimeReservationCreateCommand.builder()
                        .activity(activity)
                        .user(user)
                        .startAt(dto.getStartAt())
                        .endAt(dto.getEndAt())
                        .build()
        );

        return reservationMapper.toResponseDto(reservationRepository.save(reservation));
    }

    @Transactional
    public CourseTimeReservationResponseDto update(UUID id, CourseTimeReservationRequestDto dto, CustomUserDetails currentUser) {
        CourseTimeReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        if (!reservation.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not the owner of this reservation");
        }

        Activity activity = reservation.getActivity();
        validateOnlineCourse(activity);
        validateTimeRange(dto.getStartAt(), dto.getEndAt());
        validateSingleDuration(dto.getStartAt(), dto.getEndAt());
        validateNoOverlap(reservation.getUser().getId(), dto.getStartAt(), dto.getEndAt(), id);
        validateDailyLimit(reservation.getUser().getId(), dto.getStartAt(), dto.getEndAt(), id);

        reservation.update(CourseTimeReservationUpdateCommand.builder()
                .startAt(dto.getStartAt())
                .endAt(dto.getEndAt())
                .build());

        return reservationMapper.toResponseDto(reservation);
    }

    @Transactional
    public void delete(UUID id, CustomUserDetails currentUser) {
        CourseTimeReservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));

        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isPrivileged = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));

        if (!isOwner && !isPrivileged) {
            throw new ForbiddenException("You are not authorized to delete this reservation");
        }

        reservationRepository.delete(reservation);
    }

    @Transactional(readOnly = true)
    public List<CourseTimeReservationResponseDto> getMyReservations(CustomUserDetails currentUser, UUID activityId, LocalDate date) {
        return reservationRepository.findByUserAndFilters(currentUser.getId(), activityId, date)
                .stream()
                .map(reservationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseTimeReservationResponseDto> getByActivity(UUID activityId, UUID userId, LocalDate date) {
        return reservationRepository.findByActivityAndFilters(activityId, userId, date)
                .stream()
                .map(reservationMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateOnlineCourse(Activity activity) {
        if (activity.getActivityType() == null ||
                !"ONLINE_COURSE".equals(activity.getActivityType().getCode())) {
            throw new BadRequestException("Reservations are only available for 'online course' activities");
        }
    }

    private void validateParticipant(UUID userId, UUID activityId) {
        ActivityParticipant participant = participantRepository
                .findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new ForbiddenException("You are not a participant of this activity"));

        if (participant.getStatus() != ActivityParticipantStatus.APPROVED) {
            throw new ForbiddenException("Your participation has not been approved");
        }
    }

    private void validateTimeRange(LocalDateTime startAt, LocalDateTime endAt) {
        if (!endAt.isAfter(startAt)) {
            throw new BadRequestException("endAt must be after startAt");
        }
    }

    private void validateSingleDuration(LocalDateTime startAt, LocalDateTime endAt) {
        long minutes = Duration.between(startAt, endAt).toMinutes();
        if (minutes > MAX_SINGLE_MINUTES) {
            throw new BadRequestException("A single reservation cannot exceed 4 hours");
        }
    }

    private void validateNoOverlap(UUID userId, LocalDateTime newStart, LocalDateTime newEnd, UUID excludeId) {
        if (reservationRepository.existsOverlapping(userId, newStart, newEnd, excludeId)) {
            throw new ReservationConflictException("The requested time slot overlaps with an existing reservation");
        }
    }

    private void validateDailyLimit(UUID userId, LocalDateTime newStart, LocalDateTime newEnd, UUID excludeId) {
        // Determine the KST date of the new reservation's start
        LocalDate kstDate = newStart.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(java.time.ZoneId.of("Asia/Seoul"))
                .toLocalDate();

        long existingMinutes = reservationRepository.sumMinutesOnKstDate(userId, kstDate, excludeId);
        long newMinutes = Duration.between(newStart, newEnd).toMinutes();

        if (existingMinutes + newMinutes > MAX_DAILY_MINUTES) {
            throw new BadRequestException(
                    "Total reservations for the day cannot exceed 6 hours. " +
                    "Already reserved: " + existingMinutes + " minutes");
        }
    }
}
