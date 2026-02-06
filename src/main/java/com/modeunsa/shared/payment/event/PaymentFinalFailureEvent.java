package com.modeunsa.shared.payment.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;

public record PaymentFinalFailureEvent(PaymentDto payment, String traceId)
    implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentFinalFailureEvent";

  public PaymentFinalFailureEvent(PaymentDto payment) {
    this(payment, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
