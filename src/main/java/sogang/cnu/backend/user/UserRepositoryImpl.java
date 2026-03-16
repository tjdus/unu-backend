package sogang.cnu.backend.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import sogang.cnu.backend.quarter.QQuarter;
import sogang.cnu.backend.role.QRole;
import sogang.cnu.backend.user_role.QUserRole;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> search(String role, Boolean isCurrentQuarterActive, String joinedQuarter, String name, String studentId) {
        QUser user = QUser.user;
        QUserRole userRole = QUserRole.userRole;
        QRole qRole = QRole.role;
        QQuarter quarter = QQuarter.quarter;

        return queryFactory.selectFrom(user)
                .distinct()
                .leftJoin(user.joinedQuarter, quarter)
                .leftJoin(user.userRoles, userRole)
                .leftJoin(userRole.role, qRole)
                .where(
                        isCurrentQuarterActive != null ? user.isCurrentQuarterActive.eq(isCurrentQuarterActive) : null,
                        name != null ? user.name.containsIgnoreCase(name) : null,
                        studentId != null ? user.studentId.contains(studentId) : null,
                        joinedQuarter != null ? quarter.name.containsIgnoreCase(joinedQuarter) : null,
                        role != null ? qRole.name.containsIgnoreCase(role) : null
                )
                .fetch();
    }
}
