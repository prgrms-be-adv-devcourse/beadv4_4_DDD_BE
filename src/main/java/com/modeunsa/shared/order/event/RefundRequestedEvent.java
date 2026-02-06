package com.modeunsa.shared.order.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.order.dto.OrderDto;

public record RefundRequestedEvent(OrderDto orderDto, String traceId) implements TraceableEvent {

  public static final String EVENT_NAME = "RefundRequestedEvent";

  public RefundRequestedEvent(OrderDto orderDto) {
    this(orderDto, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
