package com.modeunsa.boundedcontext.payment.app.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ConfirmPaymentRequest(
    @NotBlank String paymentKey,
    @NotBlank String orderId,
    @Positive long amount,
    @NotBlank String pgCustomerName,
    @NotBlank String pgCustomerEmail) {}
