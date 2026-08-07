package sogang.cnu.backend.portfolio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.BadRequestException;
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
import sogang.cnu.backend.util.SecurityUtils;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

        PortfolioCreateCommand command = buildCreateCommand(dto, thumbnailUrl);
        validateQuarterRange(command.getStartQuarter(), command.getEndQuarter());
        Portfolio portfolio = Portfolio.create(command);
        Portfolio saved = portfolioRepository.save(portfolio);

        imageService.syncImages(saved.getId(), PostType.PORTFOLIO, dto.getDescription(), thumbnailUrl);

        return toResponseDtoWithContributors(saved);
    }

    @Transactional
    public PortfolioResponseDto update(UUID id, PortfolioRequestDto dto) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        SecurityUtils.requireOwnerOrAdmin(portfolio.getCreatedBy(), "본인이 작성한 글만 수정할 수 있습니다.");

        String thumbnailUrl = imageService.syncImages(
                id, PostType.PORTFOLIO, dto.getDescription(), dto.getThumbnailUrl()
        );

        PortfolioUpdateCommand command = buildUpdateCommand(dto, thumbnailUrl);
        validateQuarterRange(command.getStartQuarter(), command.getEndQuarter());
        portfolio.update(command);
        return toResponseDtoWithContributors(portfolio);
    }

    @Transactional
    public void delete(UUID id) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Portfolio not found"));
        SecurityUtils.requireOwnerOrAdmin(portfolio.getCreatedBy(), "본인이 작성한 글만 삭제할 수 있습니다.");
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

    private void validateQuarterRange(Quarter startQuarter, Quarter endQuarter) {
        if (startQuarter == null || endQuarter == null) return;
        int startOrder = startQuarter.getYear() * 10 + startQuarter.getSeason().getOrder();
        int endOrder = endQuarter.getYear() * 10 + endQuarter.getSeason().getOrder();
        if (endOrder < startOrder) {
            throw new BadRequestException("종료 분기는 시작 분기보다 이전일 수 없습니다.");
        }
    }

    private List<PortfolioContributor> resolveContributors(List<PortfolioRequestDto.ContributorRequestDto> contributors) {
        if (contributors == null) return List.of();
        return contributors.stream()
                .map(c -> {
                    boolean isMember = c.getUserId() != null && !c.getUserId().isBlank();
                    return PortfolioContributor.builder()
                            .userId(isMember ? UUID.fromString(c.getUserId()) : null)
                            .name(isMember ? null : c.getName())
                            .role(c.getRole())
                            .build();
                })
                // 학회원도 아니고 이름도 없는 빈 항목은 저장하지 않는다.
                .filter(c -> c.getUserId() != null
                        || (c.getName() != null && !c.getName().isBlank()))
                .collect(Collectors.toList());
    }

    private PortfolioResponseDto toResponseDtoWithContributors(Portfolio portfolio) {
        PortfolioResponseDto dto = portfolioMapper.toResponseDto(portfolio);

        List<PortfolioContributor> contributors = portfolio.getContributors();
        if (contributors != null && !contributors.isEmpty()) {
            List<UUID> ids = contributors.stream()
                    .map(PortfolioContributor::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<UUID, String> nameMap = ids.isEmpty()
                    ? Map.of()
                    : userRepository.findAllById(ids).stream()
                            .filter(u -> u.getName() != null)
                            .collect(Collectors.toMap(User::getId, User::getName));

            List<PortfolioResponseDto.ContributorDto> result = new ArrayList<>();
            for (int i = 0; i < contributors.size(); i++) {
                PortfolioContributor c = contributors.get(i);
                boolean isMember = c.getUserId() != null;
                result.add(PortfolioResponseDto.ContributorDto.builder()
                        // 외부 인원은 userId가 없으므로 목록 내 위치로 키를 만든다.
                        .id(isMember ? c.getUserId().toString() : "ext:" + i)
                        .userId(isMember ? c.getUserId().toString() : null)
                        .name(isMember
                                ? nameMap.getOrDefault(c.getUserId(), "Unknown")
                                : c.getName())
                        .role(c.getRole())
                        .build());
            }
            dto.setContributors(result);
        } else {
            dto.setContributors(List.of());
        }

        return dto;
    }
}
