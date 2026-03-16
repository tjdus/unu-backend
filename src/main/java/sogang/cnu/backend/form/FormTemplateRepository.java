package sogang.cnu.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, UUID> {
}

