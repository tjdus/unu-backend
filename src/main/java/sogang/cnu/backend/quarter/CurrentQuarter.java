package sogang.cnu.backend.quarter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CurrentQuarter {
    @Id
    private UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @ManyToOne
    @JoinColumn(name = "quarter_id", referencedColumnName = "id")
    private Quarter quarter;

    public void update(Quarter quarter) {
        this.quarter = quarter;
    }
}
