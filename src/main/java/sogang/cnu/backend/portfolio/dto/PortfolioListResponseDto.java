package sogang.cnu.backend.portfolio.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortfolioListResponseDto {
    private List<PortfolioResponseDto> portfolios;
    private long total;
}
