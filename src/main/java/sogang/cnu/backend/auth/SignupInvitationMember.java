package sogang.cnu.backend.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.application.Application;
import sogang.cnu.backend.user.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "signup_invitation_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"invitation_id", "student_id"})
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SignupInvitationMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invitation_id", nullable = false)
    private SignupInvitation invitation;

    @Column(name = "student_id", nullable = false, length = 8)
    private String studentId;

    @Column(length = 50)
    private String name;

    @Column(length = 100)
    private String major;

    @Column(name = "sub_major", length = 100)
    private String subMajor;

    @Column(length = 255)
    private String email;

    @Column(name = "github_id", length = 100)
    private String githubId;

    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private Instant usedAt;

    public static SignupInvitationMember create(SignupInvitation invitation, String studentId) {
        return SignupInvitationMember.builder()
                .invitation(invitation)
                .studentId(studentId)
                .build();
    }

    public static SignupInvitationMember createFromApplication(
            SignupInvitation invitation,
            Application application
    ) {
        SignupInvitationMember member = SignupInvitationMember.builder()
                .invitation(invitation)
                .studentId(application.getStudentId())
                .build();
        member.updateProfile(application);
        return member;
    }

    public void updateProfile(Application application) {
        this.name = application.getName();
        this.major = application.getMajor();
        this.subMajor = application.getSubMajor();
        this.email = application.getEmail();
        this.githubId = application.getGithubId();
        this.phoneNumber = application.getPhoneNumber();
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markUsed(User user, Instant usedAt) {
        this.user = user;
        this.usedAt = usedAt;
    }
}
