package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "form_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode schema;


    public void update(String title, String description, JsonNode schema) {
        this.title = title;
        this.description = description;
        this.schema = schema;
    }

    public static FormTemplate create(String title, String description, JsonNode schema) {
        return FormTemplate.builder()
                .title(title)
                .description(description)
                .schema(schema)
                .build();
    }
}

