package sogang.cnu.backend.user;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<User> search(String role, Boolean isActive, String joinedQuarter, String name, String studentId) {
        QUser user = QUser.user;

        return queryFactory.selectFrom(user)
                .where(
                        isActive != null ? user.isActive.eq(isActive) : null,
                        name != null ? user.name.containsIgnoreCase(name) : null,
                        studentId != null ? user.studentId.eq(studentId) : null
                )
                .fetch();
    }
}
