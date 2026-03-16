package sogang.cnu.backend.quarter.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CurrentQuarterRequestDto {
    private UUID quarterId;
}
