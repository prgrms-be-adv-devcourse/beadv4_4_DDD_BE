package com.modeunsa.boundedcontext.settlement.domain;

import java.math.BigDecimal;

public record PayoutAmounts(BigDecimal sellerAmount, BigDecimal feeAmount) {}
