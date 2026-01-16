package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class PaymentAccountDepositRequest {

  @NotNull private final Long memberId;
  @NotNull @Positive private final BigDecimal amount;
  @NotNull private final PaymentEventType paymentEventType;
}
