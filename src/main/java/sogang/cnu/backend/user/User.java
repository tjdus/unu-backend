package sogang.cnu.backend.user;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.user.command.UserCreateCommand;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name="is_active")
    @Builder.Default
    private Boolean isActive = true;

//    @Column(name = "joined_at", nullable = false)
//    private LocalDateTime joinedAt;priv

//    @ManyToOne
//    @JoinColumn(name = "joined_quarter", nullable = false)
//    private Quarter joinedQuarter;

    //TODO
    //department
    //joinedQuarter

    public static User create(UserCreateCommand command) {
        return User.builder()
                .name(command.getName())
                .username(command.getUsername())
                .password(command.getPassword())
                .studentId(command.getStudentId())
                .githubId(command.getGithubId())
                .phoneNumber(command.getPhoneNumber())
                .email(command.getEmail())
                .isActive(true)
                .build();
    }
}