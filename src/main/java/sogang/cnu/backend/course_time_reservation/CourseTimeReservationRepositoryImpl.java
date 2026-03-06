package sogang.cnu.backend.course_time_reservation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static sogang.cnu.backend.course_time_reservation.QCourseTimeReservation.courseTimeReservation;

@Repository
@RequiredArgsConstructor
public class CourseTimeReservationRepositoryImpl implements CourseTimeReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsOverlapping(UUID userId, LocalDateTime newStart, LocalDateTime newEnd, UUID excludeId) {
        Long count = queryFactory
                .select(courseTimeReservation.count())
                .from(courseTimeReservation)
                .where(
                        courseTimeReservation.user.id.eq(userId),
                        excludeId != null ? courseTimeReservation.id.ne(excludeId) : null,
                        courseTimeReservation.startAt.lt(newEnd),
                        courseTimeReservation.endAt.gt(newStart)
                )
                .fetchOne();
        return count != null && count > 0;
    }

    @Override
    public long sumMinutesOnKstDate(UUID userId, LocalDate kstDate, UUID excludeId) {
        // Convert KST date to UTC range: KST = UTC+9
        LocalDateTime utcStart = kstDate.atStartOfDay().minusHours(9);
        LocalDateTime utcEnd = kstDate.plusDays(1).atStartOfDay().minusHours(9);

        List<CourseTimeReservation> reservations = queryFactory
                .selectFrom(courseTimeReservation)
                .join(courseTimeReservation.activity).fetchJoin()
                .join(courseTimeReservation.activity.activityType).fetchJoin()
                .where(
                        courseTimeReservation.user.id.eq(userId),
                        excludeId != null ? courseTimeReservation.id.ne(excludeId) : null,
                        courseTimeReservation.startAt.goe(utcStart),
                        courseTimeReservation.startAt.lt(utcEnd),
                        courseTimeReservation.activity.activityType.code.eq("ONLINE_COURSE")
                )
                .fetch();

        return reservations.stream()
                .mapToLong(r -> java.time.Duration.between(r.getStartAt(), r.getEndAt()).toMinutes())
                .sum();
    }

    @Override
    public List<CourseTimeReservation> findByUserAndFilters(UUID userId, UUID activityId, LocalDate kstDate) {
        return queryFactory
                .selectFrom(courseTimeReservation)
                .where(
                        courseTimeReservation.user.id.eq(userId),
                        activityId != null ? courseTimeReservation.activity.id.eq(activityId) : null,
                        kstDate != null ? dateFilter(kstDate) : null
                )
                .orderBy(courseTimeReservation.startAt.asc())
                .fetch();
    }

    @Override
    public List<CourseTimeReservation> findByActivityAndFilters(UUID activityId, UUID userId, LocalDate kstDate) {
        return queryFactory
                .selectFrom(courseTimeReservation)
                .where(
                        courseTimeReservation.activity.id.eq(activityId),
                        userId != null ? courseTimeReservation.user.id.eq(userId) : null,
                        kstDate != null ? dateFilter(kstDate) : null
                )
                .orderBy(courseTimeReservation.startAt.asc())
                .fetch();
    }

    private com.querydsl.core.types.dsl.BooleanExpression dateFilter(LocalDate kstDate) {
        LocalDateTime utcStart = kstDate.atStartOfDay().minusHours(9);
        LocalDateTime utcEnd = kstDate.plusDays(1).atStartOfDay().minusHours(9);
        return courseTimeReservation.startAt.goe(utcStart)
                .and(courseTimeReservation.startAt.lt(utcEnd));
    }
}
