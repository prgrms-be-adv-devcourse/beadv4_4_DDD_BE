package com.modeunsa.shared.payment.event;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentSuccessEvent {
  private final Long orderId;
  private final BigDecimal pgPaymentAmount;
}
