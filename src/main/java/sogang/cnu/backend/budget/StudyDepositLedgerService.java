package sogang.cnu.backend.budget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.budget.dto.StudyDepositLedgerEntryDto;
import sogang.cnu.backend.quarter.CurrentQuarterService;
import sogang.cnu.backend.quarter.Quarter;
import sogang.cnu.backend.quarter.QuarterRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StudyDepositLedgerService {

    private final StudyDepositLedgerEntryRepository ledgerRepository;
    private final CurrentQuarterService currentQuarterService;
    private final QuarterRepository quarterRepository;
    private final BudgetService budgetService;

    /** 보증금 신청 시 INCOME_STUDY_DEPOSIT 1건 idempotent 생성 */
    public void recordDeposit(ActivityParticipant participant) {
        recordEntry(participant, BudgetCategory.INCOME_STUDY_DEPOSIT, depositAmount(participant));
    }

    /** 수료 확정 시 EXPENSE_STUDY_DEPOSIT_REFUND 1건 idempotent 생성 (전액 환급, 음수) */
    public void recordRefund(ActivityParticipant participant) {
        recordEntry(participant, BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND, -depositAmount(participant));
    }

    /** 수료 취소(completed: true→false) 시 환급 항목 제거 */
    public void voidRefund(ActivityParticipant participant) {
        voidEntry(participant, BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND);
    }

    /** 참여자 행 자체가 삭제될 때 수입/환급 항목 모두 제거 */
    public void voidAllForParticipant(ActivityParticipant participant) {
        voidEntry(participant, BudgetCategory.INCOME_STUDY_DEPOSIT);
        voidEntry(participant, BudgetCategory.EXPENSE_STUDY_DEPOSIT_REFUND);
    }

    /**
     * 활동이 삭제될 때 그 활동 참여자들의 원장 행을 모두 제거한다.
     * 참여자는 Activity에서 cascade 삭제되지만 원장 FK가 이를 막으므로 활동 삭제 전에 먼저 호출해야 한다.
     */
    public void voidAllForActivity(UUID activityId) {
        List<StudyDepositLedgerEntry> entries = ledgerRepository.findByActivityId(activityId);
        if (entries.isEmpty()) {
            return;
        }
        // 삭제 전에 영향받는 (분기, 월, 카테고리) 조합을 모아둔다 (삭제 후에는 조회할 수 없다)
        Set<MonthlyCategory> affected = entries.stream()
                .map(e -> new MonthlyCategory(e.getQuarter().getId(), e.getMonth(), e.getCategory()))
                .collect(Collectors.toSet());

        ledgerRepository.deleteAll(entries);
        ledgerRepository.flush();

        affected.forEach(key -> resync(key.quarterId(), key.month(), key.category()));
    }

    private record MonthlyCategory(UUID quarterId, Integer month, BudgetCategory category) {}

    @Transactional(readOnly = true)
    public List<StudyDepositLedgerEntryDto> getDetail(UUID quarterId, Integer month, BudgetCategory category) {
        return ledgerRepository.findDetail(quarterId, month, category).stream()
                .map(StudyDepositLedgerEntryDto::from)
                .collect(Collectors.toList());
    }

    private long depositAmount(ActivityParticipant participant) {
        Integer amount = participant.getActivity().getDepositAmount();
        return amount == null ? 0L : amount.longValue();
    }

    private void recordEntry(ActivityParticipant participant, BudgetCategory category, long amount) {
        if (amount == 0L) {
            return;
        }
        if (ledgerRepository.findByActivityParticipantIdAndCategory(participant.getId(), category).isPresent()) {
            return; // idempotent: 재신청/중복 호출에도 안전
        }
        UUID quarterId = currentQuarterService.getCurrentQuarterId();
        if (quarterId == null) {
            log.warn("현재 분기가 설정되지 않아 스터디 보증금 가계부 항목을 기록하지 않음. participantId={}, category={}",
                    participant.getId(), category);
            return; // 참여 신청/수료 처리 자체는 막지 않는다
        }
        Quarter quarter = quarterRepository.findById(quarterId)
                .orElseThrow(() -> new IllegalStateException("Quarter not found: " + quarterId));
        int month = LocalDate.now().getMonthValue();

        StudyDepositLedgerEntry entry = StudyDepositLedgerEntry.builder()
                .activityParticipant(participant)
                .category(category)
                .amount(amount)
                .quarter(quarter)
                .month(month)
                .occurredAt(LocalDateTime.now())
                .build();
        ledgerRepository.saveAndFlush(entry);
        resync(quarterId, month, category);
    }

    private void voidEntry(ActivityParticipant participant, BudgetCategory category) {
        ledgerRepository.findByActivityParticipantIdAndCategory(participant.getId(), category)
                .ifPresent(entry -> {
                    UUID quarterId = entry.getQuarter().getId();
                    Integer month = entry.getMonth();
                    ledgerRepository.delete(entry);
                    ledgerRepository.flush();
                    resync(quarterId, month, category);
                });
    }

    private void resync(UUID quarterId, Integer month, BudgetCategory category) {
        long total = ledgerRepository.sumAmount(quarterId, month, category);
        budgetService.syncCategoryActualAmount(quarterId, month, category, total);
    }
}
