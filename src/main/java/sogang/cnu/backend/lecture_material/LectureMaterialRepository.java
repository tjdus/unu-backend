package sogang.cnu.backend.lecture_material;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LectureMaterialRepository extends JpaRepository<LectureMaterial, UUID> {
    List<LectureMaterial> findAllByOrderByCreatedAtDesc();

    List<LectureMaterial> findAllByActivityIdOrderByCreatedAtDesc(UUID activityId);

    Optional<LectureMaterial> findFirstByActivityIdAndPrimaryTrue(UUID activityId);

    void deleteByActivityIdAndPrimaryTrue(UUID activityId);

    @Modifying
    @Query("UPDATE LectureMaterial material SET material.activity = null WHERE material.activity.id = :activityId")
    void detachFromActivity(@Param("activityId") UUID activityId);
}
