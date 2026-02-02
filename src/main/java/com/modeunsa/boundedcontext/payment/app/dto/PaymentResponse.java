package com.modeunsa.boundedcontext.payment.app.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentResponse(
    Long buyerId,
    String orderNo,
    Long orderId,
    BigDecimal totalAmount,
    boolean needsPgPayment,
    BigDecimal requestPgAmount) {

  public static PaymentResponse needPgPayment(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.buyerId())
        .orderNo(result.orderNo())
        .orderId(result.orderId())
        .totalAmount(result.totalAmount())
        .needsPgPayment(true)
        .requestPgAmount(result.requestPgAmount())
        .build();
  }

  public static PaymentResponse complete(PaymentProcessContext result) {
    return PaymentResponse.builder()
        .buyerId(result.buyerId())
        .orderNo(result.orderNo())
        .orderId(result.orderId())
        .totalAmount(result.totalAmount())
        .needsPgPayment(false)
        .requestPgAmount(BigDecimal.ZERO)
        .build();
  }
}
