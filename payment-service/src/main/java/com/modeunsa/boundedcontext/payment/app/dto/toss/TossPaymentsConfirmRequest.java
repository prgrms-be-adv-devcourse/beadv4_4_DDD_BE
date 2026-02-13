package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentProcessContext;
import lombok.Builder;

@Builder
public record TossPaymentsConfirmRequest(String paymentKey, String orderId, long amount) {

  public static TossPaymentsConfirmRequest from(PaymentProcessContext context) {
    return TossPaymentsConfirmRequest.builder()
        .paymentKey(context.paymentKey())
        .orderId(context.pgOrderId())
        .amount(context.pgAmount())
        .build();
  }
}
