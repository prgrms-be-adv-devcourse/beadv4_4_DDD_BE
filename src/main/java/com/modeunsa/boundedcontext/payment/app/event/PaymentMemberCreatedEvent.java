package com.modeunsa.boundedcontext.payment.app.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;

public record PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId, String traceId)
    implements TraceableEvent {

  public PaymentMemberCreatedEvent(@JsonProperty("member_id") Long memberId) {
    this(memberId, EventUtils.extractTraceId());
  }
}
