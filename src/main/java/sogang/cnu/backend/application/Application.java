package sogang.cnu.backend.application;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import sogang.cnu.backend.application.command.ApplicationCreateCommand;
import sogang.cnu.backend.application.command.ApplicationUpdateCommand;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.recruitment.Recruitment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(name = "applicant_user_id")
    private UUID applicantUserId;

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

    /** 신입 지원서 개인정보 수집·이용 동의 시각 (학회 내부 신청은 null). */
    @Column(name = "privacy_agreed_at")
    private LocalDateTime privacyAgreedAt;

    public void updateStatus(ApplicationStatus newStatus) {
        this.status = newStatus;
        // 합격·불합격 결정 시각: 불합격자 개인정보 파기 기한의 기준이 된다.
        if (newStatus == ApplicationStatus.PASSED || newStatus == ApplicationStatus.REJECTED) {
            this.reviewedAt = LocalDateTime.now();
        }
    }

    public void updateAnswers(JsonNode newAnswers) {
        this.answers = newAnswers;
    }

    public static Application create(ApplicationCreateCommand command) {
        return Application.builder()
                .recruitment(command.getRecruitment())
                .applicantUserId(command.getApplicantUserId())
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
                .privacyAgreedAt(command.getPrivacyAgreedAt())
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
        this.answers = command.getAnswers();
    }
}
