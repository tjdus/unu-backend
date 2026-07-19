package sogang.cnu.backend.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.portfolio.command.PortfolioCreateCommand;
import sogang.cnu.backend.portfolio.command.PortfolioUpdateCommand;
import sogang.cnu.backend.portfolio.dto.PortfolioListResponseDto;
import sogang.cnu.backend.portfolio.dto.PortfolioRequestDto;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;

    @Transactional(readOnly = true)
    public PortfolioListResponseDto getAll() {
        List<PortfolioResponseDto> portfolios = portfolioRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(portfolioMapper::toResponseDto)
                .collect(Collectors.toList());
        return PortfolioListResponseDto.builder()
                .portfolios(portfolios)
                .total(portfolios.size())
                .build();
    }

    @Transactional(readOnly = true)
    public PortfolioResponseDto getById(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        return portfolioMapper.toResponseDto(portfolio);
    }

    @Transactional
    public PortfolioResponseDto create(PortfolioRequestDto dto) {
        PortfolioCreateCommand command = toCreateCommand(dto);
        Portfolio portfolio = Portfolio.create(command);
        Portfolio saved = portfolioRepository.save(portfolio);
        return portfolioMapper.toResponseDto(saved);
    }

    @Transactional
    public PortfolioResponseDto update(UUID id, PortfolioRequestDto dto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        portfolio.update(toUpdateCommand(dto));
        return portfolioMapper.toResponseDto(portfolio);
    }

    @Transactional
    public void delete(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        portfolioRepository.delete(portfolio);
    }

    private PortfolioCreateCommand toCreateCommand(PortfolioRequestDto dto) {
        return PortfolioCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .team(dto.getTeam())
                .year(dto.getYear())
                .images(toImageEntities(dto.getImages()))
                .tags(toTagEntities(dto.getTags()))
                .build();
    }

    private PortfolioUpdateCommand toUpdateCommand(PortfolioRequestDto dto) {
        return PortfolioUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .team(dto.getTeam())
                .year(dto.getYear())
                .images(toImageEntities(dto.getImages()))
                .tags(toTagEntities(dto.getTags()))
                .build();
    }

    private List<PortfolioImage> toImageEntities(List<String> urls) {
        if (urls == null) return List.of();
        AtomicInteger order = new AtomicInteger(0);
        return urls.stream()
                .map(url -> PortfolioImage.builder()
                        .url(url)
                        .sortOrder(order.getAndIncrement())
                        .build())
                .collect(Collectors.toList());
    }

    private List<PortfolioTag> toTagEntities(List<String> names) {
        if (names == null) return List.of();
        return names.stream()
                .map(name -> PortfolioTag.builder()
                        .name(name)
                        .build())
                .collect(Collectors.toList());
    }
}
