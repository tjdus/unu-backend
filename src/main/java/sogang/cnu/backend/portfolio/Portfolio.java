package sogang.cnu.backend.portfolio;

import jakarta.persistence.*;
import lombok.*;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.portfolio.command.PortfolioCreateCommand;
import sogang.cnu.backend.portfolio.command.PortfolioUpdateCommand;

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
    private String team;
    private int year;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PortfolioTag> tags = new ArrayList<>();

    public void update(PortfolioUpdateCommand command) {
        this.title = command.getTitle();
        this.description = command.getDescription();
        this.thumbnailUrl = command.getThumbnailUrl();
        this.team = command.getTeam();
        this.year = command.getYear();

        this.images.clear();
        if (command.getImages() != null) {
            command.getImages().forEach(img -> {
                img.setPortfolio(this);
                this.images.add(img);
            });
        }

        this.tags.clear();
        if (command.getTags() != null) {
            command.getTags().forEach(tag -> {
                tag.setPortfolio(this);
                this.tags.add(tag);
            });
        }
    }

    public static Portfolio create(PortfolioCreateCommand command) {
        Portfolio portfolio = Portfolio.builder()
                .title(command.getTitle())
                .description(command.getDescription())
                .thumbnailUrl(command.getThumbnailUrl())
                .team(command.getTeam())
                .year(command.getYear())
                .build();

        if (command.getImages() != null) {
            command.getImages().forEach(img -> {
                img.setPortfolio(portfolio);
                portfolio.getImages().add(img);
            });
        }

        if (command.getTags() != null) {
            command.getTags().forEach(tag -> {
                tag.setPortfolio(portfolio);
                portfolio.getTags().add(tag);
            });
        }

        return portfolio;
    }
}
