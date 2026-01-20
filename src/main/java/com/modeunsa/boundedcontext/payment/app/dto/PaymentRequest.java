package com.modeunsa.boundedcontext.payment.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PaymentRequest(
    @NotNull Long orderId,
    @NotBlank String orderNo,
    @NotNull Long buyerId,
    @Positive BigDecimal totalAmount) {}
