package com.modeunsa.boundedcontext.payment.app.dto.member;

import java.math.BigDecimal;

public record PaymentMemberResponse(
    String customerKey, String customerEmail, String customerName, BigDecimal balance) {}
