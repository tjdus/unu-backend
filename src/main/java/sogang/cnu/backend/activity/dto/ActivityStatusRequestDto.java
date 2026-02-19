package sogang.cnu.backend.activity.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class ActivityStatusRequestDto {
    private String status;
}
