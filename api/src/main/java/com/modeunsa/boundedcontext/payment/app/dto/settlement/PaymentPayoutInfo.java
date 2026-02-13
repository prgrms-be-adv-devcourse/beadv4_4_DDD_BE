package com.modeunsa.boundedcontext.payment.app.dto.settlement;

import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentPayoutInfo(
    Long settlementId,
    Long payeeId,
    BigDecimal amount,
    PayoutEventType payoutEventType,
    LocalDateTime payoutAt) {}
