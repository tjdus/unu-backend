package sogang.cnu.backend.activity_participant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ActivityParticipantRefundAccountDto {
    private UUID participantId;
    private UUID userId;
    private String userName;
    private String studentId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private LocalDateTime paymentConfirmedAt;
}
