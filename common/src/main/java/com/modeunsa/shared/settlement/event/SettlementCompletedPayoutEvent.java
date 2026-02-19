package com.modeunsa.shared.settlement.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.settlement.dto.SettlementCompletedPayoutDto;
import java.util.List;

public record SettlementCompletedPayoutEvent(
    String traceId, String batchId, String eventId, List<SettlementCompletedPayoutDto> payouts)
    implements TraceableEvent {
  public static final String EVENT_NAME = "SettlementCompletedPayoutEvent";

  public SettlementCompletedPayoutEvent(
      String batchId, String eventId, List<SettlementCompletedPayoutDto> payouts) {
    this(EventUtils.extractTraceId(), batchId, eventId, payouts);
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
