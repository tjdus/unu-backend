package sogang.cnu.backend.activity_type;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
import sogang.cnu.backend.activity_type.command.ActivityTypeCreateCommand;
import sogang.cnu.backend.activity_type.command.ActivityTypeUpdateCommand;

@Entity
@Table(name = "activity_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    public void update(ActivityTypeUpdateCommand command) {
        this.name = command.getName();
        this.code = command.getCode();
    }

    public static ActivityType create(ActivityTypeCreateCommand command) {
        return ActivityType.builder()
                .name(command.getName())
                .code(command.getCode())
                .build();
    }
}
