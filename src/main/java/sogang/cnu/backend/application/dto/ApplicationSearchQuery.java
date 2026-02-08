package sogang.cnu.backend.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApplicationSearchQuery {
    private String name;
    private String email;
}
