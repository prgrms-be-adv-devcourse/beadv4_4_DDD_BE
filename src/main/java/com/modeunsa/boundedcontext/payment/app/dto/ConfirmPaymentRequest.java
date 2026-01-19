package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConfirmPaymentRequest(
    @NotNull Long memberId,
    @NotBlank String paymentKey,
    @NotBlank String orderId,
    @Positive long amount,
    @NotBlank String pgCustomerName,
    @NotBlank String pgCustomerEmail) {}
