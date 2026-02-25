package com.modeunsa.shared.member.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record MemberDeliveryAddressAddedEvent(
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

  public static final String EVENT_NAME = "MemberDeliveryAddressAddedEvent";

  public MemberDeliveryAddressAddedEvent(
      Long memberId,
      Long deliveryAddressId,
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName,
      boolean isDefault) {
    this(
        memberId,
        deliveryAddressId,
        recipientName,
        recipientPhone,
        zipCode,
        address,
        addressDetail,
        addressName,
        isDefault,
        EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
