package sogang.cnu.backend.portfolio.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PortfolioRequestDto {
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<String> images;
    private List<String> tags;
    private String team;
    private int year;
}
