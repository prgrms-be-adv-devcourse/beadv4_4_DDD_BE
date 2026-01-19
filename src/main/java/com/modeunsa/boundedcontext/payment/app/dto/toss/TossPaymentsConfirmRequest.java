package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import lombok.Builder;

@Builder
public record TossPaymentsConfirmRequest(String paymentKey, String orderId, long amount) {

  public static TossPaymentsConfirmRequest from(ConfirmPaymentRequest confirmPaymentRequest) {
    return TossPaymentsConfirmRequest.builder()
        .paymentKey(confirmPaymentRequest.paymentKey())
        .orderId(confirmPaymentRequest.orderId())
        .amount(confirmPaymentRequest.amount())
        .build();
  }
}
