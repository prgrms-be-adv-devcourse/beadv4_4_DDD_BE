package com.modeunsa.shared.payment.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentDto {
  private final Long orderId;
  private final String orderNo;
  private final Long buyerId;
  private final BigDecimal pgPaymentAmount;
}
