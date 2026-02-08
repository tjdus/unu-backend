package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Type;
import sogang.cnu.backend.common.domain.BaseEntity;

@Entity
@Table(name = "forms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FormTemplate template;

    @Column(nullable = false)
    private String title;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode schema;

    public void update(String title, JsonNode schema) {
        this.title = title;
        this.schema = schema;
    }

    public static Form create(FormTemplate template, String title, JsonNode schema) {
        return Form.builder()
                .template(template)
                .title(title)
                .schema(schema)
                .build();
    }
}
