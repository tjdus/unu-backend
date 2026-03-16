package sogang.cnu.backend.form;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Type;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.recruitment.Recruitment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "forms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private FormTemplate template;

    @Builder.Default
    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recruitment> recruitments = new ArrayList<>();

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private JsonNode schema;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    public void update(String title, String description, JsonNode schema, LocalDateTime startAt, LocalDateTime endAt) {
        this.title = title;
        this.description = description;
        this.schema = schema;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Form create(FormTemplate template, String title, String description, JsonNode schema, LocalDateTime startAt, LocalDateTime endAt) {
        return Form.builder()
                .template(template)
                .title(title)
                .description(description)
                .schema(schema)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }

    public static Form create(String title, String description, JsonNode schema, LocalDateTime startAt, LocalDateTime endAt) {
        return Form.builder()
                .title(title)
                .description(description)
                .schema(schema)
                .startAt(startAt)
                .endAt(endAt)
                .build();
    }
}
