package sogang.cnu.backend.budget;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudyDepositLedgerEntryRepository extends JpaRepository<StudyDepositLedgerEntry, UUID> {

    Optional<StudyDepositLedgerEntry> findByActivityParticipantIdAndCategory(
            UUID activityParticipantId, BudgetCategory category);

    void deleteByActivityParticipantId(UUID activityParticipantId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM StudyDepositLedgerEntry e " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month AND e.category = :category")
    long sumAmount(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month,
            @Param("category") BudgetCategory category
    );

    @Query("SELECT e FROM StudyDepositLedgerEntry e " +
            "JOIN FETCH e.activityParticipant p " +
            "JOIN FETCH p.activity a " +
            "JOIN FETCH p.user u " +
            "WHERE e.quarter.id = :quarterId AND e.month = :month AND e.category = :category " +
            "ORDER BY e.occurredAt ASC")
    List<StudyDepositLedgerEntry> findDetail(
            @Param("quarterId") UUID quarterId,
            @Param("month") Integer month,
            @Param("category") BudgetCategory category
    );
}
