package com.modeunsa.boundedcontext.payment.app.dto;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentAccountDepositRequest(
    @NotNull Long memberId,
    @NotNull @Positive BigDecimal amount,
    @NotNull PaymentEventType paymentEventType) {}
