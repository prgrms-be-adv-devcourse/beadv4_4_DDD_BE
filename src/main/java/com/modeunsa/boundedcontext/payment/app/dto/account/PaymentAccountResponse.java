package com.modeunsa.boundedcontext.payment.app.dto.account;

import java.math.BigDecimal;

public record PaymentAccountResponse(Long memberId, BigDecimal balance) {}
