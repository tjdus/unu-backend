package sogang.cnu.backend.activity_participant.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActivityJoinRequestDto {
    private String refundBankName;
    private String refundAccountNumber;
    private String refundAccountHolder;
    private Boolean agreedToDepositPolicy;
    private Boolean confirmedDepositPayment;
    private Boolean agreedToPromotion;
}
