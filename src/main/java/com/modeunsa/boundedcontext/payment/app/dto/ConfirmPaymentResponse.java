package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ConfirmPaymentResponse(@NotBlank String orderNo) {
  public static ConfirmPaymentResponse complete(String orderNo) {
    return ConfirmPaymentResponse.builder().orderNo(orderNo).build();
  }
}
