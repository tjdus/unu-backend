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
    private UUID userId;
    private String role;
}
