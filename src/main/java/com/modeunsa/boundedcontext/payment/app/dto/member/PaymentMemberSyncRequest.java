package com.modeunsa.boundedcontext.payment.app.dto.member;

import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;

public record PaymentMemberSyncRequest(Long id, String email, String name, MemberStatus status) {}
