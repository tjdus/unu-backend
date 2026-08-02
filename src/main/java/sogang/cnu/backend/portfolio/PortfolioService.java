package sogang.cnu.backend.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.image.ImageService;
import sogang.cnu.backend.image.PostType;
import sogang.cnu.backend.portfolio.command.PortfolioCreateCommand;
import sogang.cnu.backend.portfolio.command.PortfolioUpdateCommand;
import sogang.cnu.backend.portfolio.dto.PortfolioListResponseDto;
import sogang.cnu.backend.portfolio.dto.PortfolioRequestDto;
import sogang.cnu.backend.portfolio.dto.PortfolioResponseDto;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;
import sogang.cnu.backend.util.SecurityUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    private final ImageService imageService;
    private final QuarterRepository quarterRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PortfolioListResponseDto getAll() {
        List<PortfolioResponseDto> portfolios = portfolioRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponseDtoWithContributors)
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
        return toResponseDtoWithContributors(portfolio);
    }

    @Transactional
    public PortfolioResponseDto create(PortfolioRequestDto dto) {
        String thumbnailUrl = imageService.syncImages(
                null, PostType.PORTFOLIO, dto.getDescription(), dto.getThumbnailUrl()
        );

        Portfolio portfolio = Portfolio.create(buildCreateCommand(dto, thumbnailUrl));
        Portfolio saved = portfolioRepository.save(portfolio);

        imageService.syncImages(saved.getId(), PostType.PORTFOLIO, dto.getDescription(), thumbnailUrl);

        return toResponseDtoWithContributors(saved);
    }

    @Transactional
    public PortfolioResponseDto update(UUID id, PortfolioRequestDto dto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        SecurityUtils.requireOwner(portfolio.getCreatedBy(), "본인이 작성한 글만 수정할 수 있습니다.");

        String thumbnailUrl = imageService.syncImages(
                id, PostType.PORTFOLIO, dto.getDescription(), dto.getThumbnailUrl()
        );

        portfolio.update(buildUpdateCommand(dto, thumbnailUrl));
        return toResponseDtoWithContributors(portfolio);
    }

    @Transactional
    public void delete(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        SecurityUtils.requireOwnerOrManager(portfolio.getCreatedBy(), "삭제 권한이 없습니다.");
        imageService.deletePostImages(id, PostType.PORTFOLIO);
        portfolioRepository.delete(portfolio);
    }

    @Transactional
    public PortfolioResponseDto setPinned(UUID id, boolean pinned) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        portfolio.setPinned(pinned);
        return toResponseDtoWithContributors(portfolio);
    }

    private PortfolioCreateCommand buildCreateCommand(PortfolioRequestDto dto, String thumbnailUrl) {
        return PortfolioCreateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .startQuarter(resolveQuarter(dto.getStartQuarterId()))
                .endQuarter(resolveQuarter(dto.getEndQuarterId()))
                .contributors(resolveContributors(dto.getContributors()))
                .build();
    }

    private PortfolioUpdateCommand buildUpdateCommand(PortfolioRequestDto dto, String thumbnailUrl) {
        return PortfolioUpdateCommand.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .startQuarter(resolveQuarter(dto.getStartQuarterId()))
                .endQuarter(resolveQuarter(dto.getEndQuarterId()))
                .contributors(resolveContributors(dto.getContributors()))
                .build();
    }

    private Quarter resolveQuarter(String quarterId) {
        if (quarterId == null || quarterId.isBlank()) return null;
        return quarterRepository.findById(UUID.fromString(quarterId)).orElse(null);
    }

    private List<PortfolioContributor> resolveContributors(List<PortfolioRequestDto.ContributorRequestDto> contributors) {
        if (contributors == null) return List.of();
        return contributors.stream()
                .map(c -> PortfolioContributor.builder()
                        .userId(UUID.fromString(c.getUserId()))
                        .role(c.getRole())
                        .build())
                .collect(Collectors.toList());
    }

    private PortfolioResponseDto toResponseDtoWithContributors(Portfolio portfolio) {
        PortfolioResponseDto dto = portfolioMapper.toResponseDto(portfolio);

        List<PortfolioContributor> contributors = portfolio.getContributors();
        if (contributors != null && !contributors.isEmpty()) {
            List<UUID> ids = contributors.stream()
                    .map(PortfolioContributor::getUserId)
                    .collect(Collectors.toList());
            Map<UUID, String> nameMap = userRepository.findAllById(ids).stream()
                    .collect(Collectors.toMap(User::getId, User::getName));
            dto.setContributors(contributors.stream()
                    .map(c -> PortfolioResponseDto.ContributorDto.builder()
                            .id(c.getUserId().toString())
                            .name(nameMap.getOrDefault(c.getUserId(), "Unknown"))
                            .role(c.getRole())
                            .build())
                    .collect(Collectors.toList()));
        } else {
            dto.setContributors(List.of());
        }

        return dto;
    }
}
