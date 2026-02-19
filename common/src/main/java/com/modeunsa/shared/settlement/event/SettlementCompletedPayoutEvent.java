package com.modeunsa.shared.settlement.event;

import com.modeunsa.shared.settlement.dto.SettlementCompletedPayoutDto;
import java.util.List;

public record SettlementCompletedPayoutEvent(
    String batchId, String eventId, List<SettlementCompletedPayoutDto> payouts) {}
