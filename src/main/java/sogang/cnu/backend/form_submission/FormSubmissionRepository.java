package sogang.cnu.backend.form_submission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormSubmissionRepository extends JpaRepository<FormSubmission, UUID> {
    List<FormSubmission> findByFormId(UUID formId);
    List<FormSubmission> findByUserId(UUID userId);
    void deleteByFormId(UUID formId);
}
