package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record SellerRegisteredEvent(
    Long memberId,
    Long memberSellerId,
    String businessName,
    String representativeName,
    String settlementBankName,
    String settlementBankAccount,
    String status,
    String traceId)
    implements TraceableEvent {

  private static final String EVENT_NAME = "SellerRegisteredEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
