package sogang.cnu.backend.activity_type;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sogang.cnu.backend.activity_type.dto.ActivityTypeRequestDto;

@Entity
@Table(name = "activity_types")
@Getter
@Setter
public class ActivityType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    public void update(ActivityTypeRequestDto requestDto) {
        this.name = requestDto.getName();
    }
}
