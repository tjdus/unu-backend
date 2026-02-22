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

    public void update(ActivityTypeUpdateCommand command) {
        this.name = command.getName();
    }

    public static ActivityType create(ActivityTypeCreateCommand command) {
        ActivityType activityType = ActivityType.builder()
                .name(command.getName())
                .build();
        return activityType;
    }
}
