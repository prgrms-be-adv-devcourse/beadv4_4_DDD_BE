package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record MemberDeliveryAddressDeletedEvent(
    Long memberId, Long deliveryAddressId, String traceId) implements TraceableEvent {

  public static final String EVENT_NAME = "MemberDeliveryAddressDeletedEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
