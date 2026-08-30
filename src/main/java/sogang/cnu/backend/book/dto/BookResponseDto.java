package sogang.cnu.backend.book.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class BookResponseDto {
    private UUID id;
    private String title;
    private String author;
    private String publisher;
    private String description;
    private Integer quantity;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
