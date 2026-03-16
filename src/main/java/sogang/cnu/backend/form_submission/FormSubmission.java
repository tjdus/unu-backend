package sogang.cnu.backend.form_submission;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.form.Form;
import sogang.cnu.backend.form_submission.command.FormSubmissionCreateCommand;
import sogang.cnu.backend.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "form_submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", nullable = false)
    private JsonNode answers;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", nullable = false)
    private JsonNode formSnapshot;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    public static FormSubmission create(FormSubmissionCreateCommand command) {
        return FormSubmission.builder()
                .form(command.getForm())
                .user(command.getUser())
                .answers(command.getAnswers())
                .formSnapshot(command.getFormSnapshot())
                .submittedAt(LocalDateTime.now())
                .build();
    }
}
