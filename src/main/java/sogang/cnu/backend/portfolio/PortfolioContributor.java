package sogang.cnu.backend.portfolio;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioContributor {
    // 학회원 기여자는 userId만, 외부 인원은 name만 채워진다.
    private UUID userId;
    private String name;
    private String role;
}
