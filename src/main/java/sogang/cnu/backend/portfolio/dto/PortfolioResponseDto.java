package sogang.cnu.backend.portfolio.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class PortfolioResponseDto {
    private UUID id;
    private String title;
    private String description;
    private String thumbnailUrl;
    private List<String> images;
    private List<String> tags;
    private String team;
    private int year;
    private String createdAt;
}
