package sogang.cnu.backend.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.portfolio.dto.PortfolioListResponseDto;
import sogang.cnu.backend.portfolio.dto.PortfolioRequestDto;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/portfolios")
@RequiredArgsConstructor
public class PortfolioPublicController {

    private final PortfolioService portfolioService;

    @GetMapping("")
    public ResponseEntity<PortfolioListResponseDto> getAll() {
        return ResponseEntity.ok(portfolioService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(portfolioService.getById(id));
    }
}
