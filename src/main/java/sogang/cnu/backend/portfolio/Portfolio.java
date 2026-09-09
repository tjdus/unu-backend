package sogang.cnu.backend.portfolio;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.portfolio.command.PortfolioCreateCommand;
import sogang.cnu.backend.portfolio.command.PortfolioUpdateCommand;
import sogang.cnu.backend.quarter.Quarter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "portfolios")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "start_quarter_id")
    private Quarter startQuarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "end_quarter_id")
    private Quarter endQuarter;

    @ElementCollection
    @CollectionTable(name = "portfolio_contributors", joinColumns = @JoinColumn(name = "portfolio_id"))
    @Builder.Default
    private List<PortfolioContributor> contributors = new ArrayList<>();

    @Column(nullable = false, columnDefinition = "boolean not null default false")
    @Builder.Default
    private Boolean pinned = false;

    public void update(PortfolioUpdateCommand command) {
        this.title = command.getTitle();
        this.description = command.getDescription();
        this.thumbnailUrl = command.getThumbnailUrl();
        this.startQuarter = command.getStartQuarter();
        this.endQuarter = command.getEndQuarter();
        this.contributors.clear();
        if (command.getContributors() != null) {
            this.contributors.addAll(command.getContributors());
        }
    }

    public static Portfolio create(PortfolioCreateCommand command) {
        return Portfolio.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .thumbnailUrl(command.getThumbnailUrl())
                .startQuarter(command.getStartQuarter())
                .endQuarter(command.getEndQuarter())
                .contributors(command.getContributors() != null
                        ? new ArrayList<>(command.getContributors())
                        : new ArrayList<>())
                .build();
    }
}
