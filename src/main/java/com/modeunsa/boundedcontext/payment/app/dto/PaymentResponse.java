package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentResponse {
  private final Long buyerId;
  private final String orderNo;
  private final Long orderId;
  private final BigDecimal totalAmount;
}
