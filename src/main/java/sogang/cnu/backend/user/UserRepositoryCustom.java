package sogang.cnu.backend.user;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> search(String role, Boolean isCurrentQuarterActive, String joinedQuarter, String name, String studentId);
}
