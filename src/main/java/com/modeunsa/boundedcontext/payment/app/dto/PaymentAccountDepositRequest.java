package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentAccountDepositRequest {

  @NotNull private final Long memberId;
  @Positive private final long amount;
  @NotNull private final PaymentEventType paymentEventType;

  public BigDecimal convertAmountToBigDecimal() {
    return BigDecimal.valueOf(amount);
  }
}
