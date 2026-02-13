package com.modeunsa.boundedcontext.payment.app.dto.member;

import com.modeunsa.boundedcontext.payment.domain.types.PaymentMemberStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentMemberSyncRequest(
    @NotNull Long id,
    @NotEmpty String email,
    @NotEmpty String name,
    @NotNull PaymentMemberStatus status) {}
