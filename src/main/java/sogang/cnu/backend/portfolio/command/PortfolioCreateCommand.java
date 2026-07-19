package sogang.cnu.backend.portfolio.command;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.portfolio.PortfolioImage;
import sogang.cnu.backend.portfolio.PortfolioTag;

import java.util.List;

@Getter
@Builder
public class PortfolioCreateCommand {
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<PortfolioImage> images;
    private List<PortfolioTag> tags;
    private String team;
    private int year;
}