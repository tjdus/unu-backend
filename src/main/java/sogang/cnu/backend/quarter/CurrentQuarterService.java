package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.dto.CurrentQuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

@Service
@RequiredArgsConstructor
public class CurrentQuarterService {
    private final CurrentQuarterRepository currentQuarterRepository;
    private final QuarterRepository quarterRepository;
    private final QuarterMapper quarterMapper;

    private CurrentQuarter getOne() {
        return currentQuarterRepository.findById(1L)
                .orElseGet(() -> {
                    CurrentQuarter cq = new CurrentQuarter(1L, null);
                    return currentQuarterRepository.save(cq);
                });
    }

    @Transactional
    public QuarterResponseDto get() {
        Quarter quarter = getOne().getQuarter();
        return quarterMapper.toResponseDto(quarter);
    }

    @Transactional
    public QuarterResponseDto update(CurrentQuarterRequestDto requestDto) {
        CurrentQuarter currentQuarter = getOne();
        Quarter quarter = quarterRepository.findById(requestDto.getQuarterId())
                .orElseThrow(() -> new NotFoundException("Quarter not found"));

        currentQuarter.update(quarter);
        return quarterMapper.toResponseDto(currentQuarter.getQuarter());
    }

}
