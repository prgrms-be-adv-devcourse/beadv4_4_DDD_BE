package com.modeunsa.shared.payment.event;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentFailedEvent {
  private final String resultCode;
  private final String msg;
  private final Long orderId;
  private final BigDecimal pgPaymentAmount;
  private final BigDecimal shortFailAmount;
}
