package sogang.cnu.backend.user;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.user.command.UserCreateCommand;
import sogang.cnu.backend.user.command.UserUpdateCommand;
import sogang.cnu.backend.user_role.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    @Column(name = "student_id", nullable = false, unique = true)
    private String studentId;

    @Column(name = "github_id", unique = true)
    private String githubId;

    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    private String major;

    private String subMajor;

    @Column(unique = true)
    private String email;

    @Column(nullable = false, name="is_current_quarter_active")
    @Builder.Default
    private Boolean isCurrentQuarterActive = false;

    @ManyToOne
    @JoinColumn(name = "joined_quarter_id")
    private Quarter joinedQuarter;

    @Column(nullable = false)
    @Builder.Default
    private MemberStatus memberStatus = MemberStatus.MEMBER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAlumni = false;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    public void update(UserUpdateCommand command) {
        this.name = command.getName();
        this.username = command.getUsername();
        this.studentId = command.getStudentId();
        this.githubId = command.getGithubId();
        this.phoneNumber = command.getPhoneNumber();
        this.email = command.getEmail();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateActiveStatus(Boolean isCurrentQuarterActive) {
        this.isCurrentQuarterActive = isCurrentQuarterActive;
    }

    public static User create(UserCreateCommand command) {
        return User.builder()
                .name(command.getName())
                .username(command.getUsername())
                .password(command.getPassword())
                .studentId(command.getStudentId())
                .githubId(command.getGithubId())
                .phoneNumber(command.getPhoneNumber())
                .email(command.getEmail())
                .isCurrentQuarterActive(true)
                .joinedQuarter(command.getJoinedQuarter())
                .build();
    }
}