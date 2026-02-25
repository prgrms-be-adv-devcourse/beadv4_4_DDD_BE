package com.modeunsa.boundedcontext.payment.app.dto.payout;

import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentPayoutDto(
    Long id,
    Long payeeId,
    LocalDateTime payoutDate,
    BigDecimal amount,
    PayoutEventType payoutEventType) {}
