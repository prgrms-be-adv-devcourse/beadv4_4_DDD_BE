package com.modeunsa.shared.order.event;

import com.modeunsa.global.event.EventUtils;
import com.modeunsa.global.event.TraceableEvent;
import com.modeunsa.shared.order.dto.OrderItemDto;
import java.util.List;

public record OrderCancellationConfirmedEvent(List<OrderItemDto> orderItemDto, String traceId)
    implements TraceableEvent {
  public static final String EVENT_NAME = "OrderCanceledEvent";

  public OrderCancellationConfirmedEvent(List<OrderItemDto> orderItemDto) {
    this(orderItemDto, EventUtils.extractTraceId());
  }

  @Override
  public String eventName() {
    return EVENT_NAME;
  }
}
