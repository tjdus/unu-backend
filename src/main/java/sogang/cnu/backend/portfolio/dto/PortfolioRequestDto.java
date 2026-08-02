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
    private String startQuarterId;
    private String endQuarterId;
    private List<ContributorRequestDto> contributors;

    @Getter
    @Setter
    @Builder
    public static class ContributorRequestDto {
        private String userId;
        private String role;
    }
}
