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
    private String startQuarterId;
    private String startQuarterName;
    private String endQuarterId;
    private String endQuarterName;
    private List<ContributorDto> contributors;
    private Boolean pinned;
    private String createdAt;
    private String createdBy;

    @Getter
    @Builder
    public static class ContributorDto {
        private String id;
        // 학회원이면 userId, 외부 인원이면 null
        private String userId;
        private String name;
        private String role;
    }
}
