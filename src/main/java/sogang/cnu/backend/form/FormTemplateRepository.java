package sogang.cnu.backend.form;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormTemplateRepository extends JpaRepository<FormTemplate, Long> {
}

