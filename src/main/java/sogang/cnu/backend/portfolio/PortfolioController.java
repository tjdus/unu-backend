package sogang.cnu.backend.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import sogang.cnu.backend.portfolio.dto.PortfolioListResponseDto;
import sogang.cnu.backend.portfolio.dto.PortfolioRequestDto;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PortfolioResponseDto> create(@RequestBody PortfolioRequestDto dto) {
        return ResponseEntity.ok(portfolioService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PortfolioResponseDto> update(
            @PathVariable UUID id,
            @RequestBody PortfolioRequestDto dto) {
        return ResponseEntity.ok(portfolioService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        portfolioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<PortfolioResponseDto> setPinned(
            @PathVariable UUID id, @RequestParam boolean pinned) {
        return ResponseEntity.ok(portfolioService.setPinned(id, pinned));
    }
}
