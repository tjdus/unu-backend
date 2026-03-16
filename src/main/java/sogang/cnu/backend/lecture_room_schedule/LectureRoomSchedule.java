package sogang.cnu.backend.lecture_room_schedule;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.lecture_room_schedule.command.LectureRoomScheduleCreateCommand;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.user.User;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "lecture_room_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lecture_room_schedule",
                columnNames = {"quarter_id", "day_of_week", "time_slot", "user_id"}
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LectureRoomSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static LectureRoomSchedule create(LectureRoomScheduleCreateCommand command) {
        return LectureRoomSchedule.builder()
                .quarter(command.getQuarter())
                .dayOfWeek(command.getDayOfWeek())
                .timeSlot(command.getTimeSlot())
                .user(command.getUser())
                .build();
    }
}
