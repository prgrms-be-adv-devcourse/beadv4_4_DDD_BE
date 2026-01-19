package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class PaymentResponse {
  private final Long buyerId;
  private final String orderNo;
  private final Long orderId;
  private final BigDecimal totalAmount;
  private final boolean needsCharge;
  private final BigDecimal chargeAmount;

  public static PaymentResponse needCharge(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.getBuyerId())
        .orderNo(result.getOrderNo())
        .orderId(result.getOrderId())
        .totalAmount(result.getTotalAmount())
        .needsCharge(true)
        .chargeAmount(result.getChargeAmount())
        .build();
  }

  public static PaymentResponse complete(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.getBuyerId())
        .orderNo(result.getOrderNo())
        .orderId(result.getOrderId())
        .totalAmount(result.getTotalAmount())
        .needsCharge(false)
        .chargeAmount(BigDecimal.ZERO)
        .build();
  }
}
