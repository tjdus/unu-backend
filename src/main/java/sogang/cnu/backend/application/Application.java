package sogang.cnu.backend.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import sogang.cnu.backend.application.command.ApplicationCreateCommand;
import sogang.cnu.backend.application.command.ApplicationUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.recruitment.Recruitment;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String studentId;

    @Column(nullable = false)
    private String major;

    private String subMajor;

    @Column(nullable = false)
    private String email;

    private String githubId;

    @Column(nullable = false)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", nullable = false)
    private JsonNode answers;

    @Type(JsonType.class)
    @Column(columnDefinition = "json", nullable = false)
    private JsonNode formSnapshot;

    private String password;

    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
    }

    public static Application create(ApplicationCreateCommand command) {
        return Application.builder()
                .recruitment(command.getRecruitment())
                .name(command.getName())
                .studentId(command.getStudentId())
                .major(command.getMajor())
                .subMajor(command.getSubMajor())
                .email(command.getEmail())
                .githubId(command.getGithubId())
                .phoneNumber(command.getPhoneNumber())
                .status(ApplicationStatus.APPLIED)
                .submittedAt(LocalDateTime.now())
                .answers(command.getAnswers())
                .formSnapshot(command.getFormSnapshot())
                .password(command.getPassword())
                .build();
    }

    public void update(ApplicationUpdateCommand command) {
        this.name = command.getName();
        this.studentId = command.getStudentId();
        this.major = command.getMajor();
        this.subMajor = command.getSubMajor();
        this.email = command.getEmail();
        this.githubId = command.getGithubId();
        this.phoneNumber = command.getPhoneNumber();
    }
}

