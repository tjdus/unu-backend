package sogang.cnu.backend.book;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.book.dto.BookRequestDto;
import sogang.cnu.backend.book.dto.BookResponseDto;
import sogang.cnu.backend.common.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<BookResponseDto> getAll() {
        return bookRepository.findAllByOrderByTitleAscAuthorAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BookResponseDto create(BookRequestDto request) {
        Book book = Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .publisher(normalize(request.getPublisher()))
                .description(normalize(request.getDescription()))
                .quantity(request.getQuantity())
                .note(normalize(request.getNote()))
                .build();
        return toResponse(bookRepository.save(book));
    }

    @Transactional
    public BookResponseDto update(UUID id, BookRequestDto request) {
        Book book = findById(id);
        book.update(
                request.getTitle().trim(),
                request.getAuthor().trim(),
                normalize(request.getPublisher()),
                normalize(request.getDescription()),
                request.getQuantity(),
                normalize(request.getNote())
        );
        return toResponse(book);
    }

    @Transactional
    public void delete(UUID id) {
        bookRepository.delete(findById(id));
    }

    private Book findById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("도서를 찾을 수 없습니다."));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BookResponseDto toResponse(Book book) {
        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .description(book.getDescription())
                .quantity(book.getQuantity())
                .note(book.getNote())
                .createdAt(book.getCreatedAt())
                .modifiedAt(book.getModifiedAt())
                .build();
    }
}
