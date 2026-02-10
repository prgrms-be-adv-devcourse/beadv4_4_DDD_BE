package com.modeunsa.boundedcontext.payment.app.dto.member;

import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PaymentMemberSyncRequest(
    @NotNull Long id,
    @NotNull @NotEmpty String email,
    @NotNull @NotEmpty String name,
    @NotNull MemberStatus status) {}
