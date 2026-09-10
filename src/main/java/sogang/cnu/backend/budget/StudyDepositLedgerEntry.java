package sogang.cnu.backend.budget;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.common.domain.BaseEntity;
import sogang.cnu.backend.quarter.Quarter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 참여자별 스터디 보증금 납부/환급 상세 내역 (참여자별 줄 단위 기록).
 * BudgetItem.actualAmount(INCOME_STUDY_DEPOSIT / EXPENSE_STUDY_DEPOSIT_REFUND)는
 * 이 테이블의 합계를 반영하는 파생 캐시로 취급한다.
 */
@Entity
@Table(
        name = "study_deposit_ledger_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_study_deposit_ledger_participant_category",
                columnNames = {"activity_participant_id", "category"}
        )
)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudyDepositLedgerEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_participant_id", nullable = false)
    private ActivityParticipant activityParticipant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetCategory category; // INCOME_STUDY_DEPOSIT | EXPENSE_STUDY_DEPOSIT_REFUND 전용

    @Column(nullable = false)
    private Long amount; // 지출은 음수 (BudgetItem 부호 규칙과 동일)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quarter_id", nullable = false)
    private Quarter quarter;

    @Column(nullable = false)
    private Integer month; // 1~12

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
