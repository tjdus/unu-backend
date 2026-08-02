package sogang.cnu.backend.image;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findByPostIdAndPostType(UUID postId, PostType postType);

    List<Image> findByUrlInAndStatus(Collection<String> urls, ImageStatus status);

    List<Image> findByStatusAndCreatedAtBefore(ImageStatus status, LocalDateTime before);

    Optional<Image> findByUrl(String url);
}
