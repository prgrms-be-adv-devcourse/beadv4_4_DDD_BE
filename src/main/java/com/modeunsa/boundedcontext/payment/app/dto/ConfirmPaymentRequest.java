package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ConfirmPaymentRequest(
    @NotBlank String paymentKey,
    @NotBlank @Pattern(regexp = "^[0-9]+$", message = "orderId는 숫자 형식이어야 합니다") String orderId,
    @Positive long amount,
    @NotBlank String pgCustomerName,
    @NotBlank String pgCustomerEmail) {}
