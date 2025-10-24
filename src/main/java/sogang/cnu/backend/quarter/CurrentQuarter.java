package sogang.cnu.backend.quarter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
    private Long id = 1L;

    @ManyToOne
    @JoinColumn(name = "quarter_id", referencedColumnName = "id")
    private Quarter quarter;

    public void update(Quarter quarter) {
        this.quarter = quarter;
    }
}
