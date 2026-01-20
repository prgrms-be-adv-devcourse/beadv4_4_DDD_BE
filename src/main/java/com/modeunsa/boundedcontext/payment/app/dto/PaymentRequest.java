package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class PaymentRequest {
  private Long orderId;
  private String orderNo;
  private Long buyerId;
  private Long sellerId;
  private BigDecimal pgPaymentAmount;
  private BigDecimal salePrice;

  public boolean isPositive() {
    return pgPaymentAmount != null && pgPaymentAmount.compareTo(BigDecimal.ZERO) > 0;
  }
}
