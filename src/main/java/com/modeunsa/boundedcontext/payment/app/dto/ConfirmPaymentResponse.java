package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmPaymentResponse(@NotBlank String orderNo) {}
