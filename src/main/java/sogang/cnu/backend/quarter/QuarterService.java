package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
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
        Quarter quarter = quarterMapper.toEntity(dto);
        Quarter savedQuarter = quarterRepository.save(quarter);
        return quarterMapper.toResponseDto(savedQuarter);
    }

    public QuarterResponseDto update(Long id, QuarterRequestDto dto) {
        Quarter quarter = quarterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Quarter not found"));

        quarter.update(dto);

        Quarter updatedQuarter = quarterRepository.save(quarter);
        return quarterMapper.toResponseDto(updatedQuarter);
    }

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


}
