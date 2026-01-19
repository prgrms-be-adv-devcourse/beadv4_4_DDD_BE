package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;

public record TossPaymentsConfirmRequest(String paymentKey, String orderId, long amount) {

  public TossPaymentsConfirmRequest(ConfirmPaymentRequest confirmPaymentRequest) {
    this(
        confirmPaymentRequest.paymentKey(),
        confirmPaymentRequest.orderId(),
        confirmPaymentRequest.amount());
  }
}
