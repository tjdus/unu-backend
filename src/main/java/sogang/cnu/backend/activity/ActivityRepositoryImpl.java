package sogang.cnu.backend.activity;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sogang.cnu.backend.activity.dto.ActivitySearchQuery;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ActivityRepositoryImpl implements ActivityRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Activity> search(ActivitySearchQuery query) {
        QActivity activity = QActivity.activity;

        return queryFactory.selectFrom(activity)
                .where(
                        query.getTitle() != null ? activity.title.containsIgnoreCase(query.getTitle()) : null,
                        query.getStatus() != null ? activity.status.eq(ActivityStatus.valueOf(query.getStatus())) : null,
                        query.getActivityTypeId() != null ? activity.activityType.id.eq(query.getActivityTypeId()) : null,
                        query.getQuarterId() != null ? activity.quarter.id.eq(query.getQuarterId()) : null
                )
                .fetch();
    }
}
