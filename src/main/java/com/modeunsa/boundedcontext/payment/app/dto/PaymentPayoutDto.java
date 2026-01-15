package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.PayoutEventType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class PaymentPayoutDto {
  private final Long id;
  private final Long payeeId;
  private final LocalDateTime payoutDate;
  private final BigDecimal amount;
  private final PayoutEventType payoutEventType;
}
