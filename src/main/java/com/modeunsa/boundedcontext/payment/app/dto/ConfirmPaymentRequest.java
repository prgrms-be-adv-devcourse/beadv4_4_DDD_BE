package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ConfirmPaymentRequest(
    @NotBlank String paymentKey, @Positive BigDecimal totalAmount) {}
