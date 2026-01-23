package com.modeunsa.shared.payment.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.payment.dto.PaymentDto;

public record PaymentSuccessEvent(PaymentDto payment, String traceId) implements TraceableEvent {

  public PaymentSuccessEvent(PaymentDto payment) {
    this(payment, EventUtils.extractTraceId());
  }
}
