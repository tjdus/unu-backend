package sogang.cnu.backend.course_time_reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.course_time_reservation.dto.CourseTimeReservationRequestDto;
import sogang.cnu.backend.course_time_reservation.dto.CourseTimeReservationResponseDto;
import sogang.cnu.backend.security.CurrentUser;
import sogang.cnu.backend.security.CustomUserDetails;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CourseTimeReservationController {

    private final CourseTimeReservationService reservationService;

    @PostMapping("/api/course-reservations")
    public ResponseEntity<CourseTimeReservationResponseDto> create(
            @RequestBody CourseTimeReservationRequestDto dto,
            @CurrentUser CustomUserDetails currentUser
    ) {
        return ResponseEntity.status(201).body(reservationService.create(dto, currentUser));
    }

    @PutMapping("/api/course-reservations/{id}")
    public ResponseEntity<CourseTimeReservationResponseDto> update(
            @PathVariable UUID id,
            @RequestBody CourseTimeReservationRequestDto dto,
            @CurrentUser CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(reservationService.update(id, dto, currentUser));
    }

    @DeleteMapping("/api/course-reservations/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @CurrentUser CustomUserDetails currentUser
    ) {
        reservationService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/course-reservations/me")
    public ResponseEntity<List<CourseTimeReservationResponseDto>> getMyReservations(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(required = false) UUID activityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(reservationService.getMyReservations(currentUser, activityId, date));
    }

    @GetMapping("/api/activities/{activityId}/course-reservations")
    public ResponseEntity<List<CourseTimeReservationResponseDto>> getByActivity(
            @PathVariable UUID activityId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(reservationService.getByActivity(activityId, userId, date));
    }
}
