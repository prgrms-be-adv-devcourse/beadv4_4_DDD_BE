package com.modeunsa.shared.payment.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId, String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentMemberCreatedEvent";

  public PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId) {
    this(memberId, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
