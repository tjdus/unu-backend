package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.budget.dto.BudgetExpenseEntryDto;
import sogang.cnu.backend.budget.dto.BudgetExpenseEntryRequestDto;
import sogang.cnu.backend.common.exception.BadRequestException;
import sogang.cnu.backend.common.exception.NotFoundException;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;

import java.util.List;
import java.util.UUID;

/**
 * 지출 건별 상세 내역(인강 구매비·엠티·개총/종총·간식비·기타) CRUD.
 * 쓰기마다 그 (분기, 월, 카테고리)의 합계를 예산안 항목의 예상·실제 금액에 다시 반영한다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BudgetExpenseEntryService {

    private static final int LABEL_MAX_LENGTH = 100;
    private static final int NOTE_MAX_LENGTH = 200;

    private final BudgetExpenseEntryRepository entryRepository;
    private final QuarterRepository quarterRepository;
    private final BudgetService budgetService;

    @Transactional(readOnly = true)
    public List<BudgetExpenseEntryDto> getEntries(UUID quarterId, Integer month, BudgetCategory category) {
        validateCategory(category);
        validateMonth(month);
        return entryRepository.findDetail(quarterId, month, category).stream()
                .map(BudgetExpenseEntryDto::from)
                .toList();
    }

    public BudgetExpenseEntryDto create(BudgetExpenseEntryRequestDto request) {
        validateCategory(request.getCategory());
        validateMonth(request.getMonth());
        Quarter quarter = findQuarter(request.getQuarterId());

        BudgetExpenseEntry entry = BudgetExpenseEntry.builder()
                .quarter(quarter)
                .month(request.getMonth())
                .category(request.getCategory())
                .label(normalizeLabel(request.getLabel()))
                .plannedAmount(normalizeAmount(request.getPlannedAmount(), "예상 금액"))
                .actualAmount(normalizeAmount(request.getActualAmount(), "실제 금액"))
                .occurredAt(request.getOccurredAt())
                .note(normalizeNote(request.getNote()))
                .build();
        entryRepository.saveAndFlush(entry);

        resync(quarter.getId(), entry.getMonth(), entry.getCategory());
        return BudgetExpenseEntryDto.from(entry);
    }

    public BudgetExpenseEntryDto update(UUID id, BudgetExpenseEntryRequestDto request) {
        BudgetExpenseEntry entry = findEntry(id);
        entry.update(
                normalizeLabel(request.getLabel()),
                normalizeAmount(request.getPlannedAmount(), "예상 금액"),
                normalizeAmount(request.getActualAmount(), "실제 금액"),
                request.getOccurredAt(),
                normalizeNote(request.getNote())
        );
        entryRepository.flush();

        resync(entry.getQuarter().getId(), entry.getMonth(), entry.getCategory());
        return BudgetExpenseEntryDto.from(entry);
    }

    public void delete(UUID id) {
        BudgetExpenseEntry entry = findEntry(id);
        UUID quarterId = entry.getQuarter().getId();
        Integer month = entry.getMonth();
        BudgetCategory category = entry.getCategory();

        entryRepository.delete(entry);
        entryRepository.flush();

        resync(quarterId, month, category);
    }

    /** 예산안 항목의 예상·실제를 건별 합계로 다시 맞춘다 (BudgetService.create/update에서도 호출) */
    public void resync(UUID quarterId, Integer month, BudgetCategory category) {
        long planned = entryRepository.sumPlanned(quarterId, month, category);
        long actual = entryRepository.sumActual(quarterId, month, category);
        budgetService.syncCategoryAmounts(quarterId, month, category, planned, actual);
    }

    private BudgetExpenseEntry findEntry(UUID id) {
        return entryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("상세 내역을 찾을 수 없습니다."));
    }

    private Quarter findQuarter(UUID quarterId) {
        if (quarterId == null) {
            throw new BadRequestException("분기를 선택해주세요.");
        }
        return quarterRepository.findById(quarterId)
                .orElseThrow(() -> new NotFoundException("분기를 찾을 수 없습니다."));
    }

    private void validateCategory(BudgetCategory category) {
        if (category == null || !BudgetCategory.DETAIL_MANAGED_CATEGORIES.contains(category)) {
            throw new BadRequestException("상세 내역으로 관리하는 항목이 아닙니다.");
        }
    }

    private void validateMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            throw new BadRequestException("월은 1~12 사이여야 합니다.");
        }
    }

    private String normalizeLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new BadRequestException("항목명을 입력해주세요.");
        }
        String trimmed = label.trim();
        if (trimmed.length() > LABEL_MAX_LENGTH) {
            throw new BadRequestException("항목명은 " + LABEL_MAX_LENGTH + "자 이하로 입력해주세요.");
        }
        return trimmed;
    }

    private String normalizeNote(String note) {
        if (note == null || note.isBlank()) return null;
        String trimmed = note.trim();
        if (trimmed.length() > NOTE_MAX_LENGTH) {
            throw new BadRequestException("비고는 " + NOTE_MAX_LENGTH + "자 이하로 입력해주세요.");
        }
        return trimmed;
    }

    /** 지출이므로 양수로 들어와도 음수로 저장한다 (BudgetItem 부호 규칙) */
    private long normalizeAmount(Long amount, String fieldName) {
        if (amount == null) {
            throw new BadRequestException(fieldName + "을(를) 입력해주세요.");
        }
        return -Math.abs(amount);
    }
}
