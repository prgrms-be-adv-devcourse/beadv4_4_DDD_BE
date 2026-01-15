package com.modeunsa.shared.settlement.event;

import com.modeunsa.shared.settlement.dto.SettlementMemberDto;

public record SettlementMemberCreatedEvent(SettlementMemberDto settlementMemberDto) {}
