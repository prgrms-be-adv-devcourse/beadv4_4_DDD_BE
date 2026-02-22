package com.modeunsa.shared.settlement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementCompletedPayoutDto(
    Long settlementId,
    Long payeeId,
    BigDecimal amount,
    String payoutEventType,
    LocalDateTime payoutAt) {}
