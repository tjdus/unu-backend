package sogang.cnu.backend.quarter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.dto.CurrentQuarterRequestDto;
import sogang.cnu.backend.quarter.dto.QuarterResponseDto;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentQuarterService {
    private final CurrentQuarterRepository currentQuarterRepository;
    private final QuarterRepository quarterRepository;
    private final QuarterMapper quarterMapper;

    private UUID FIXED_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private CurrentQuarter getOne() {
        return currentQuarterRepository.findById(FIXED_ID)
                .orElseGet(() -> {
                    CurrentQuarter cq = new CurrentQuarter(FIXED_ID, null);
                    return currentQuarterRepository.save(cq);
                });
    }

    @Transactional
    public QuarterResponseDto get() {
        Quarter quarter = getOne().getQuarter();
        return quarterMapper.toResponseDto(quarter);
    }

    /** 현재 분기의 ID. 설정된 현재 분기가 없으면 null. (readOnly 경로에서 안전하도록 생성은 하지 않는다.) */
    @Transactional(readOnly = true)
    public UUID getCurrentQuarterId() {
        return currentQuarterRepository.findById(FIXED_ID)
                .map(CurrentQuarter::getQuarter)
                .map(Quarter::getId)
                .orElse(null);
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
