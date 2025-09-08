package sogang.cnu.backend.user_role;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.role.Role;
import sogang.cnu.backend.user.User;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Role role;
}
