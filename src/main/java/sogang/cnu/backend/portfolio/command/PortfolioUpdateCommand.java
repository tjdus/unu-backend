package sogang.cnu.backend.portfolio.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.portfolio.PortfolioContributor;
import sogang.cnu.backend.quarter.Quarter;

import java.util.List;

@Getter
@Builder
public class PortfolioUpdateCommand {
    private String title;
    private String description;
    private String thumbnailUrl;
    private Quarter startQuarter;
    private Quarter endQuarter;
    private List<PortfolioContributor> contributors;
}
