package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record MemberDeliveryAddressDeletedEvent(
    Long memberId, Long deliveryAddressId, String traceId) implements TraceableEvent {

  public static final String EVENT_NAME = "MemberDeliveryAddressDeletedEvent";

  public MemberDeliveryAddressDeletedEvent(Long memberId, Long deliveryAddressId) {

    this(memberId, deliveryAddressId, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
