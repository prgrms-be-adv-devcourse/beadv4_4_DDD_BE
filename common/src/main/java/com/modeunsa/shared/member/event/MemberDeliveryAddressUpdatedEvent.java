package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.TraceableEvent;

public record MemberDeliveryAddressUpdatedEvent(
    Long memberId,
    Long deliveryAddressId,
    String recipientName,
    String recipientPhone,
    String zipCode,
    String address,
    String addressDetail,
    String addressName,
    boolean isDefault,
    String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "MemberDeliveryAddressUpdatedEvent";

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
