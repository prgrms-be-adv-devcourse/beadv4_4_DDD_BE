package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ConfirmPaymentRequest(
    @NotBlank String paymentKey, @NotBlank String orderId, @Positive long amount) {}
