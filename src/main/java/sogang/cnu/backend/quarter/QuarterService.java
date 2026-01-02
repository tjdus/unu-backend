package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.command.QuarterCreateCommand;
import sogang.cnu.backend.quarter.command.QuarterUpdateCommand;
import sogang.cnu.backend.quarter.dto.QuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuarterService {
    private final QuarterRepository quarterRepository;
    private final QuarterMapper quarterMapper;

    @Transactional(readOnly = true)
    public QuarterResponseDto getById(Long id) {
        Quarter quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));

        return quarterMapper.toResponseDto(quarter);
    }

    @Transactional(readOnly = true)
    public List<QuarterResponseDto> getAll() {
        return quarterRepository.findAll().stream()
                .map(quarterMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuarterResponseDto create(QuarterRequestDto dto) {
        Quarter quarter = Quarter.create(toCreateCommand(dto));
        Quarter savedQuarter = quarterRepository.save(quarter);
        return quarterMapper.toResponseDto(savedQuarter);
    }

    @Transactional
    public QuarterResponseDto update(Long id, QuarterRequestDto dto) {
        Quarter quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));
        QuarterUpdateCommand updateCommand = toUpdateCommand(dto);
        quarter.update(updateCommand);

        return quarterMapper.toResponseDto(quarter);
    }

    @Transactional
    public void delete(Long id) {
        Quarter quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));

        quarterRepository.delete(quarter);
    }

    @Transactional(readOnly = true)
    public List<QuarterResponseDto> searchByYearAndSeason(int year, Season season) {
        List<Quarter> quarters = quarterRepository.findByYearAndSeason(year, season);
        return quarters.stream()
                .map(quarterMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private QuarterCreateCommand toCreateCommand(QuarterRequestDto dto) {
        return QuarterCreateCommand.builder()
                .year(dto.getYear())
                .season(Season.valueOf(dto.getSeason()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    private QuarterUpdateCommand toUpdateCommand(QuarterRequestDto dto) {
        return QuarterUpdateCommand.builder()
                .year(dto.getYear())
                .season(Season.valueOf(dto.getSeason()))
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }


}
