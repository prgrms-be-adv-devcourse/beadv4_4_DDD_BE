package com.modeunsa.boundedcontext.payment.app.event;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentRequestEvent {
  private final Long orderId;
  private final String orderNo;
  private final Long buyerId;
  private final Long sellerId;
  private final BigDecimal pgPaymentAmount;
  private final BigDecimal salePrice;
}
