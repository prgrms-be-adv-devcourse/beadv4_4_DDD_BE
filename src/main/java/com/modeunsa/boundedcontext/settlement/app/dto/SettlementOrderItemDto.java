package com.modeunsa.boundedcontext.settlement.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementOrderItemDto(
    Long orderItemId,
    Long buyerMemberId,
    Long sellerMemberId,
    BigDecimal amount,
    LocalDateTime paymentAt) {}
