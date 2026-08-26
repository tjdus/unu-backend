package sogang.cnu.backend.about_example;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AboutExampleRepository extends JpaRepository<AboutExample, UUID> {
    List<AboutExample> findAllByCategoryOrderByDisplayOrderAscCreatedAtDesc(AboutExampleCategory category);
}
