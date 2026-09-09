package sogang.cnu.backend.activity_opening_period;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "activity_opening_periods",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_activity_opening_period_quarter",
                columnNames = "quarter_id"
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityOpeningPeriod extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "revision_end_at", nullable = false)
    private LocalDateTime revisionEndAt;

    @Column(nullable = false)
    private boolean enabled;

    public void update(
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime revisionEndAt,
            boolean enabled
    ) {
        this.startAt = startAt;
        this.endAt = endAt;
        this.revisionEndAt = revisionEndAt;
        this.enabled = enabled;
    }
}
