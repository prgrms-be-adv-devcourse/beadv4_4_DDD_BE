package com.modeunsa.shared.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementCompletedPayoutDto(
    Long settlementId, Long sellerMemberId, BigDecimal amount, LocalDateTime payoutAt) {}
