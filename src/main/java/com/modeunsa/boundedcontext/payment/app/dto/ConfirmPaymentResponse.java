package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentResponse(
    @NotBlank String orderNo,
    @NotBlank String paymentKey,
    @NotBlank String orderName,
    long totalAmount) {

  public ConfirmPaymentResponse(String orderNo, TossPaymentsConfirmResponse response) {
    this(orderNo, response.paymentKey(), response.orderName(), response.totalAmount());
  }
}
