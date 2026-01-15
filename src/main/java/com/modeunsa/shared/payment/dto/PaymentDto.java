package com.modeunsa.shared.payment.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentDto {
  private final Long orderId;
  private final String orderNo;
  private final Long buyerId;
  private final BigDecimal pgPaymentAmount;
}
