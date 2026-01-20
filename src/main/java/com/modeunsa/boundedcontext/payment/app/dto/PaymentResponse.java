package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentResponse(
    Long buyerId,
    String orderNo,
    Long orderId,
    BigDecimal totalAmount,
    boolean needsCharge,
    BigDecimal chargeAmount) {

  public static PaymentResponse needCharge(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.buyerId())
        .orderNo(result.orderNo())
        .orderId(result.orderId())
        .totalAmount(result.totalAmount())
        .needsCharge(true)
        .chargeAmount(result.chargeAmount())
        .build();
  }

  public static PaymentResponse complete(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.buyerId())
        .orderNo(result.orderNo())
        .orderId(result.orderId())
        .totalAmount(result.totalAmount())
        .needsCharge(false)
        .chargeAmount(BigDecimal.ZERO)
        .build();
  }
}
