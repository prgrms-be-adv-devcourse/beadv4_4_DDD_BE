package com.modeunsa.boundedcontext.settlement.app.event;

import com.modeunsa.boundedcontext.settlement.app.dto.SettlementMemberDto;

public record SettlementMemberCreatedEvent(SettlementMemberDto settlementMemberDto) {}
