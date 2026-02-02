package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PaymentRequest(
    @NotNull Long orderId,
    @NotBlank String orderNo,
    @NotNull @Positive BigDecimal totalAmount,
    @NotNull LocalDateTime paymentDeadlineAt,
    @NotNull ProviderType providerType) {}
