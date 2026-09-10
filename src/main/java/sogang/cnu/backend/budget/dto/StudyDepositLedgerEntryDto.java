package sogang.cnu.backend.budget.dto;

import lombok.Builder;
import lombok.Getter;
import sogang.cnu.backend.activity_participant.ActivityParticipant;
import sogang.cnu.backend.budget.StudyDepositLedgerEntry;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StudyDepositLedgerEntryDto {
    private UUID id;
    private UUID activityId;
    private String activityTitle;
    private UUID participantId;
    private String userName;
    private String studentId;
    private Long amount;
    private LocalDateTime occurredAt;

    public static StudyDepositLedgerEntryDto from(StudyDepositLedgerEntry entry) {
        ActivityParticipant participant = entry.getActivityParticipant();
        return StudyDepositLedgerEntryDto.builder()
                .id(entry.getId())
                .activityId(participant.getActivity().getId())
                .activityTitle(participant.getActivity().getTitle())
                .participantId(participant.getId())
                .userName(participant.getUser().getName())
                .studentId(participant.getUser().getStudentId())
                .amount(entry.getAmount())
                .occurredAt(entry.getOccurredAt())
                .build();
    }
}
