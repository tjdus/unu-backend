package sogang.cnu.backend.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.notice.dto.NoticeListResponseDto;
import sogang.cnu.backend.notice.dto.NoticeRequestDto;
import sogang.cnu.backend.notice.dto.NoticeResponseDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional(readOnly = true)
    public NoticeListResponseDto getAll(Integer limit) {
        List<Notice> notices = noticeRepository.findAllByOrderByCreatedAtDesc();
        if (limit != null && limit >= 0 && limit < notices.size()) {
            notices = notices.subList(0, limit);
        }
        List<NoticeResponseDto> dtos = notices.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
        return NoticeListResponseDto.builder()
                .notices(dtos)
                .total(dtos.size())
                .build();
    }

    @Transactional(readOnly = true)
    public NoticeResponseDto getById(UUID id) {
        return toResponseDto(findOrThrow(id));
    }

    @Transactional
    public NoticeResponseDto create(NoticeRequestDto dto) {
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .tag(dto.getTag())
                .content(dto.getContent())
                .build();
        return toResponseDto(noticeRepository.save(notice));
    }

    @Transactional
    public NoticeResponseDto update(UUID id, NoticeRequestDto dto) {
        Notice notice = findOrThrow(id);
        notice.update(dto.getTitle(), dto.getTag(), dto.getContent());
        return toResponseDto(notice);
    }

    @Transactional
    public void delete(UUID id) {
        noticeRepository.delete(findOrThrow(id));
    }

    private Notice findOrThrow(UUID id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notice not found"));
    }

    private NoticeResponseDto toResponseDto(Notice notice) {
        return NoticeResponseDto.builder()
                .id(notice.getId())
                .title(notice.getTitle())
                .tag(notice.getTag())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt() != null ? notice.getCreatedAt().toString() : null)
                .build();
    }
}
