package com.modeunsa.shared.payment.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;

public record PaymentSuccessEvent(PaymentDto payment, String traceId) implements TraceableEvent {

  public static final String EVENT_NAME = "PaymentSuccessEvent";

  public PaymentSuccessEvent(PaymentDto payment) {
    this(payment, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
