package sogang.cnu.backend.common.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseDto {
    private AuditorDto createdBy;
    private LocalDateTime createdAt;
    private AuditorDto modifiedBy;
    private LocalDateTime modifiedAt;
}
