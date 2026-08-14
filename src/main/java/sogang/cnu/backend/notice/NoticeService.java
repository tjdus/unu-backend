package sogang.cnu.backend.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.notice.dto.NoticeListResponseDto;
import sogang.cnu.backend.notice.dto.NoticeRequestDto;
import sogang.cnu.backend.notice.dto.NoticeResponseDto;
import sogang.cnu.backend.notice.dto.NoticeUnreadSummaryDto;
import sogang.cnu.backend.user.User;
import sogang.cnu.backend.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final UserRepository userRepository;

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
    public NoticeUnreadSummaryDto getUnreadSummary(UUID userId) {
        List<UUID> noticeIds = noticeReadRepository.findUnreadNoticeIds(userId);
        return NoticeUnreadSummaryDto.builder()
                .totalCount(noticeIds.size())
                .noticeIds(noticeIds)
                .build();
    }

    @Transactional
    public void markRead(UUID userId, UUID noticeId) {
        Notice notice = noticeRepository.findByIdForUpdate(noticeId)
                .orElseThrow(() -> new NotFoundException("Notice not found"));
        saveReadReceipt(userId, notice);
    }

    @Transactional
    public NoticeResponseDto create(UUID userId, NoticeRequestDto dto) {
        Notice notice = Notice.builder()
                .title(dto.getTitle())
                .tag(dto.getTag())
                .content(dto.getContent())
                .notificationEnabled(true)
                .build();
        Notice savedNotice = noticeRepository.save(notice);
        saveReadReceipt(userId, savedNotice);
        return toResponseDto(savedNotice);
    }

    @Transactional
    public NoticeResponseDto update(UUID userId, UUID id, NoticeRequestDto dto) {
        Notice notice = findOrThrow(id);
        notice.update(dto.getTitle(), dto.getTag(), dto.getContent());
        noticeReadRepository.deleteByNoticeId(id);
        saveReadReceipt(userId, notice);
        return toResponseDto(notice);
    }

    @Transactional
    public void delete(UUID id) {
        Notice notice = findOrThrow(id);
        noticeReadRepository.deleteByNoticeId(id);
        noticeRepository.delete(notice);
    }

    private void saveReadReceipt(UUID userId, Notice notice) {
        if (noticeReadRepository.existsByNoticeIdAndUserId(notice.getId(), userId)) return;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        noticeReadRepository.save(NoticeRead.builder()
                .notice(notice)
                .user(user)
                .readAt(LocalDateTime.now())
                .build());
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
