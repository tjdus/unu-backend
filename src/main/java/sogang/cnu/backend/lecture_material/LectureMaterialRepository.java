package sogang.cnu.backend.lecture_material;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, UUID> {
    List<LectureMaterial> findAllByOrderByCreatedAtDesc();
}
