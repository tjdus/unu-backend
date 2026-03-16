package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationLookupRequestDto {
    private String name;
    private String email;
}
