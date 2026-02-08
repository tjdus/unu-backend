package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import sogang.cnu.backend.common.domain.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "form_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormTemplate extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode schema;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Form> forms = new ArrayList<>();

    public void update(String title, JsonNode schema) {
        this.title = title;
        this.schema = schema;
    }

    public static FormTemplate create(String title, JsonNode schema) {
        return FormTemplate.builder()
                .title(title)
                .schema(schema)
                .build();
    }
}

